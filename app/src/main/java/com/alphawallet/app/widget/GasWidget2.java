package com.alphawallet.app.widget;

import static com.alphawallet.app.C.GAS_LIMIT_MIN;
import static com.alphawallet.ethereum.EthereumNetworkBase.MAINNET_ID;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alphawallet.app.BuildConfig;
import com.alphawallet.app.C;
import com.alphawallet.app.R;
import com.alphawallet.app.entity.EIP1559FeeOracleResult;
import com.alphawallet.app.entity.GasPriceSpread;
import com.alphawallet.app.entity.GasPriceSpread2;
import com.alphawallet.app.entity.StandardFunctionInterface;
import com.alphawallet.app.entity.tokens.Token;
import com.alphawallet.app.repository.EthereumNetworkBase;
import com.alphawallet.app.repository.TokensRealmSource;
import com.alphawallet.app.repository.entity.Realm1559Gas;
import com.alphawallet.app.repository.entity.RealmGasSpread;
import com.alphawallet.app.repository.entity.RealmTokenTicker;
import com.alphawallet.app.service.GasService;
import com.alphawallet.app.service.TickerService;
import com.alphawallet.app.service.TokensService;
import com.alphawallet.app.ui.GasSettingsActivity;
import com.alphawallet.app.ui.widget.entity.GasSpeed;
import com.alphawallet.app.ui.widget.entity.GasSpeed2;
import com.alphawallet.app.util.BalanceUtils;
import com.alphawallet.app.util.Utils;
import com.alphawallet.app.web3.entity.Web3Transaction;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmQuery;

/**
 * Created by JB on 20/01/2022.
 */
public class GasWidget2 extends LinearLayout implements Runnable
{
    private GasPriceSpread2 gasSpread;
    private Realm1559Gas realmGasSpread;
    private TokensService tokensService;
    private BigInteger customGasLimit;    //from slider
    private BigInteger initialTxGasLimit; //this is the gas limit specified from the dapp transaction.
    private BigInteger baseLineGasLimit;  //this is our candidate gas limit - either from the dapp or a default, can be replaced by accurate estimate if initialTx was zero
    private BigInteger presetGasLimit;    //this is the gas limit used for the presets. It will use, in order of priority: gas estimate from node, gas from dapp tx, calculated gas
    private BigInteger transactionValue;  //'value' base token amount from dapp transaction
    private BigInteger adjustedValue;     //adjusted value, in case we are use 'all funds' to wipe an account.
    private BigInteger initialGasPrice;   //gasprice from dapp transaction
    private Token token;
    private Activity baseActivity;
    private StandardFunctionInterface functionInterface;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private final TextView speedText;
    private final TextView timeEstimate;
    private final LinearLayout gasWarning;
    private final LinearLayout speedWarning;
    private final Context context;

    private GasPriceSpread2.TXSpeed currentGasSpeedIndex = GasPriceSpread2.TXSpeed.STANDARD;
    private long customNonce = -1;
    private boolean isSendingAll;
    private BigInteger resendGasPrice = BigInteger.ZERO;

    public GasWidget2(Context ctx, AttributeSet attrs)
    {
        super(ctx, attrs);
        inflate(ctx, R.layout.item_gas_settings, this);

        context = ctx;
        speedText = findViewById(R.id.text_speed);
        timeEstimate = findViewById(R.id.text_time_estimate);
        gasWarning = findViewById(R.id.layout_gas_warning);
        speedWarning = findViewById(R.id.layout_speed_warning);

        setOnClickListener(v -> {
            Token baseEth = tokensService.getToken(token.tokenInfo.chainId, token.getWallet());
            Intent intent = new Intent(context, GasSettingsActivity.class);
            intent.putExtra(C.EXTRA_CHAIN_ID, token.tokenInfo.chainId);
            intent.putExtra(C.EXTRA_GAS_LIMIT, baseLineGasLimit.toString());
            intent.putExtra(C.EXTRA_CUSTOM_GAS_LIMIT, customGasLimit.toString());
            intent.putExtra(C.EXTRA_GAS_LIMIT_PRESET, presetGasLimit.toString());
            intent.putExtra(C.EXTRA_TOKEN_BALANCE, baseEth.balance.toString());
            intent.putExtra(C.EXTRA_AMOUNT, transactionValue.toString());
            intent.putExtra(C.EXTRA_GAS_PRICE, gasSpread);  //Parcelised
            intent.putExtra(C.EXTRA_NONCE, customNonce);
            intent.putExtra(C.EXTRA_MIN_GAS_PRICE, resendGasPrice.longValue());
            baseActivity.startActivityForResult(intent, C.SET_GAS_SETTINGS);
        });
    }

    // Called once from ActionSheet constructor
    public void setupWidget(TokensService svs, Token t, Web3Transaction tx, StandardFunctionInterface sfi, Activity act)
    {
        tokensService = svs;
        token = t;
        initialTxGasLimit = tx.gasLimit;
        baseActivity = act;
        functionInterface = sfi;
        transactionValue = tx.value;
        adjustedValue = tx.value;
        isSendingAll = isSendingAll(tx);
        initialGasPrice = tx.gasPrice;
        customNonce = tx.nonce;

        if (tx.gasLimit.equals(BigInteger.ZERO)) //dapp didn't specify a limit, use default limits until node returns an estimate (see setGasEstimate())
        {
            baseLineGasLimit = GasService.getDefaultGasLimit(token, tx);
        }
        else
        {
            baseLineGasLimit = tx.gasLimit;
        }

        presetGasLimit = baseLineGasLimit;
        customGasLimit = baseLineGasLimit;

        setupGasSpeeds(tx);
        startGasListener();
    }

    //set custom fee if specified by tx feed
    private void setupGasSpeeds(Web3Transaction w3tx)
    {
        Realm1559Gas getGas = getGasQuery().findFirst();
        if (getGas != null)
        {
            initGasSpeeds(getGas);
        }
        else
        {
            // Couldn't get current gas. Add a blank custom gas speed node
            gasSpread = new GasPriceSpread2(getContext(), w3tx.maxFeePerGas, w3tx.maxPriorityFeePerGas);
        }

        if (w3tx.maxFeePerGas.compareTo(BigInteger.ZERO) > 0 && w3tx.maxPriorityFeePerGas.compareTo(BigInteger.ZERO) > 0)
        {
            gasSpread.setCustom(w3tx.maxFeePerGas, w3tx.maxPriorityFeePerGas, GasPriceSpread.FAST_SECONDS);
        }
    }

    private GasSpeed buildCustomGasElement(BigInteger newGasPrice, long expectedTxTime)
    {
        String customSpeedTitle;
        if (initialTxGasLimit.compareTo(BigInteger.ZERO) > 0 && initialGasPrice.compareTo(BigInteger.ZERO) > 0
                && customGasLimit.equals(initialTxGasLimit) && newGasPrice.equals(initialGasPrice))
        {
            customSpeedTitle = getContext().getString(R.string.speed_dapp);
        }
        else
        {
            customSpeedTitle = getContext().getString(R.string.speed_custom);
        }
        return new GasSpeed(customSpeedTitle, expectedTxTime, newGasPrice, true);
    }

    public void onDestroy()
    {
        if (realmGasSpread != null) realmGasSpread.removeAllChangeListeners();
    }

    /**
     * This function is the leaf for when the user clicks on a gas setting; fast, slow, custom, etc
     *
     * @param gasSelectionIndex
     * @param maxFeePerGas
     * @param maxPriorityFee
     * @param custGasLimit
     * @param expectedTxTime
     * @param nonce
     */
    public void setCurrentGasIndex(int gasSelectionIndex, BigInteger maxFeePerGas, BigInteger maxPriorityFee, BigDecimal custGasLimit, long expectedTxTime, long nonce)
    {
        if (gasSelectionIndex < GasPriceSpread2.TXSpeed.values().length)
        {
            currentGasSpeedIndex = GasPriceSpread2.TXSpeed.values()[gasSelectionIndex];
        }

        customNonce = nonce;
        customGasLimit = custGasLimit.toBigInteger();

        if (maxFeePerGas.compareTo(BigInteger.ZERO) > 0 && maxPriorityFee.compareTo(BigInteger.ZERO) > 0)
        {
            gasSpread.setCustom(maxFeePerGas, maxPriorityFee, expectedTxTime);
        }

        tokensService.track(currentGasSpeedIndex.name());
        handler.post(this);
    }

    public boolean checkSufficientGas()
    {
        BigInteger useGasLimit = getUseGasLimit();
        boolean sufficientGas = true;
        GasSpeed2 gs = gasSpread.getSelectedGasFee(currentGasSpeedIndex);
        BigDecimal networkFee = new BigDecimal(gs.gasPrice.multiply(useGasLimit));
        Token base = tokensService.getTokenOrBase(token.tokenInfo.chainId, token.getWallet());

        if (isSendingAll)
        {
            sufficientGas = token.balance.subtract(new BigDecimal(adjustedValue).add(networkFee)).compareTo(BigDecimal.ZERO) >= 0;
        }
        else if (token.isEthereum() && token.balance.subtract(new BigDecimal(transactionValue).add(networkFee)).compareTo(BigDecimal.ZERO) < 0)
        {
            sufficientGas = false;
        }
        else if (!token.isEthereum() && base.balance.subtract(networkFee).compareTo(BigDecimal.ZERO) < 0)
        {
            sufficientGas = false;
        }

        if (!sufficientGas)
        {
            gasWarning.setVisibility(View.VISIBLE);
        }
        else
        {
            gasWarning.setVisibility(View.GONE);
        }

        return sufficientGas;
    }

    private int setAverageGas()
    {
        int index = 0;
        for (int i = 0; i < gasSpeeds.size(); i++)
        {
            GasSpeed gs = gasSpeeds.get(i);
            if (gs.speed.equals(context.getString(R.string.speed_average)))
            {
                index = i;
                break;
            }
        }

        return index;
    }

    private BigInteger getUseGasLimit()
    {
        if (currentGasSpeedIndex == customGasSpeedIndex)
        {
            return customGasLimit;
        }
        else
        {
            return presetGasLimit;
        }
    }

    private BigInteger calculateSendAllValue()
    {
        BigInteger sendAllValue;
        GasSpeed gs = gasSpeeds.get(currentGasSpeedIndex);
        BigDecimal networkFee = new BigDecimal(gs.gasPrice.multiply(getUseGasLimit()));

        if (isSendingAll)
        {
            //need to recalculate the 'send all' value
            //calculate max amount possible
            sendAllValue = token.balance.subtract(networkFee).toBigInteger();
            if (sendAllValue.compareTo(BigInteger.ZERO) < 0) sendAllValue = BigInteger.ZERO;
        }
        else
        {
            sendAllValue = transactionValue;
        }

        return sendAllValue;
    }

    private RealmQuery<Realm1559Gas> getGasQuery2()
    {
        return tokensService.getTickerRealmInstance().where(Realm1559Gas.class)
                .equalTo("chainId", token.tokenInfo.chainId);
    }

    private void startGasListener()
    {
        if (realmGasSpread != null) realmGasSpread.removeAllChangeListeners();
        realmGasSpread = getGasQuery2().findFirstAsync();
        realmGasSpread.addChangeListener(realmSpread -> {
            if (realmGasSpread.isValid())
            {
                initGasSpeeds((Realm1559Gas) realmSpread);
            }
        });
    }

    private RealmQuery<Realm1559Gas> getGasQuery()
    {
        return tokensService.getTickerRealmInstance().where(Realm1559Gas.class)
                .equalTo("chainId", token.tokenInfo.chainId);
    }

    private void initGasSpeeds(Realm1559Gas gs)
    {
        try
        {
            gasSpread = new GasPriceSpread2(getContext(), gs.getResult());

            TextView editTxt = findViewById(R.id.edit_text);

            if (tokensService.hasLockedGas(token.tokenInfo.chainId) && editTxt.getVisibility() == View.VISIBLE)
            {
                findViewById(R.id.edit_text).setVisibility(View.GONE);
                setOnClickListener(null);
            }

            //if we have mainnet then show timings, otherwise no timing, if the token has fiat value, show fiat value of gas, so we need the ticker
            handler.post(this);
        }
        catch (Exception e)
        {
            currentGasSpeedIndex = 0;
            if (BuildConfig.DEBUG) e.printStackTrace();
        }
    }

    /**
     * Update the UI with the gas value and expected transaction time (if main net).
     * Note - there is no ticker listener - it's unlikely any ticker change would produce a noticeable change in the gas price
     */
    @Override
    public void run()
    {
        if (currentGasSpeedIndex == -1) currentGasSpeedIndex = 0;
        GasSpeed gs = gasSpeeds.get(currentGasSpeedIndex);

        Token baseCurrency = tokensService.getTokenOrBase(token.tokenInfo.chainId, token.getWallet());
        BigInteger networkFee = gs.gasPrice.multiply(getUseGasLimit());
        String gasAmountInBase = BalanceUtils.getScaledValueScientific(new BigDecimal(networkFee), baseCurrency.tokenInfo.decimals);
        if (gasAmountInBase.equals("0")) gasAmountInBase = "0.0001";
        String displayStr = context.getString(R.string.gas_amount, gasAmountInBase, baseCurrency.getSymbol());

        //Can we display value for gas?
        try (Realm realm = tokensService.getTickerRealmInstance())
        {
            RealmTokenTicker rtt = realm.where(RealmTokenTicker.class)
                    .equalTo("contract", TokensRealmSource.databaseKey(token.tokenInfo.chainId, "eth"))
                    .findFirst();

            if (rtt != null)
            {
                //calculate equivalent fiat
                double cryptoRate = Double.parseDouble(rtt.getPrice());
                double cryptoAmount = BalanceUtils.weiToEth(new BigDecimal(networkFee)).doubleValue();//Double.parseDouble(gasAmountInBase);
                displayStr += context.getString(R.string.gas_fiat_suffix,
                        TickerService.getCurrencyString(cryptoAmount * cryptoRate),
                        rtt.getCurrencySymbol());

                if (token.tokenInfo.chainId == MAINNET_ID && gs.seconds > 0)
                {
                    displayStr += context.getString(R.string.gas_time_suffix,
                            Utils.shortConvertTimePeriodInSeconds(gs.seconds, context));
                }
            }
        }
        catch (Exception e)
        {
            //
        }
        timeEstimate.setText(displayStr);
        speedText.setText(gs.speed);
        adjustedValue = calculateSendAllValue();

        if (isSendingAll)
        {
            functionInterface.updateAmount();
        }

        if (currentGasSpeedIndex == customGasSpeedIndex)
        {
            checkCustomGasPrice(gasSpeeds.get(customGasSpeedIndex).gasPrice);
        }
        else
        {
            speedWarning.setVisibility(View.GONE);
        }
        checkSufficientGas();
        manageWarnings();
    }

    public BigInteger getGasPrice(BigInteger defaultPrice)
    {
        if (currentGasSpeedIndex == -1)
        {
            return defaultPrice;
        }
        else
        {
            GasSpeed gs = gasSpeeds.get(currentGasSpeedIndex);
            return gs.gasPrice;
        }
    }

    public BigInteger getValue()
    {
        if (isSendingAll)
        {
            return adjustedValue;
        }
        else
        {
            return transactionValue;
        }
    }

    private void checkCustomGasPrice(BigInteger customGasPrice)
    {
        if (currentGasSpeedIndex != customGasSpeedIndex)
        {
            return;
        }
        if (gasSpeeds.size() <= 2)
        {
            return;
        }

        double dGasPrice = customGasPrice.doubleValue();

        GasSpeed ug = gasSpeeds.get(0); //rapid
        GasSpeed lg = gasSpeeds.get(gasSpeeds.size() - 2); //slow

        double lowerBound = lg.gasPrice.doubleValue();
        double upperBound = ug.gasPrice.doubleValue();

        if (resendGasPrice.compareTo(BigInteger.ZERO) > 0)
        {
            if (dGasPrice > (3.0 * resendGasPrice.doubleValue()))
            {
                showCustomSpeedWarning(true);
            }
            else
            {
                speedWarning.setVisibility(View.GONE);
            }
        }
        else if (dGasPrice < lowerBound)
        {
            showCustomSpeedWarning(false);
        }
        else if (dGasPrice > 2.0 * upperBound)
        {
            showCustomSpeedWarning(true);
        }
        else
        {
            speedWarning.setVisibility(View.GONE);
        }
    }

    private void showCustomSpeedWarning(boolean high)
    {
        if (currentGasSpeedIndex != customGasSpeedIndex)
        {
            return;
        }

        TextView warningText = findViewById(R.id.text_speed_warning);

        if (high)
        {
            warningText.setText(getResources().getString(R.string.speed_high_gas));
        }
        else
        {
            warningText.setText(getResources().getString(R.string.speed_too_low));
        }
        speedWarning.setVisibility(View.VISIBLE);
    }

    private void manageWarnings()
    {
        if (gasWarning.getVisibility() == VISIBLE || speedWarning.getVisibility() == VISIBLE)
        {
            speedText.setVisibility(View.GONE);
            if (gasWarning.getVisibility() == VISIBLE && speedWarning.getVisibility() == VISIBLE)
            {
                speedWarning.setVisibility(View.GONE);
            }
        }
        else
        {
            speedText.setVisibility(View.VISIBLE);
        }
    }

    public BigInteger getGasLimit()
    {
        return getUseGasLimit();
    }

    public long getNonce()
    {
        if (currentGasSpeedIndex == customGasSpeedIndex)
        {
            return customNonce;
        }
        else
        {
            return -1;
        }
    }

    public long getExpectedTransactionTime()
    {
        GasSpeed gs = gasSpeeds.get(currentGasSpeedIndex);
        return gs.seconds;
    }

    private boolean isSendingAll(Web3Transaction tx)
    {
        if (token.isEthereum())
        {
            //gas fee:
            BigDecimal networkFee = new BigDecimal(tx.gasPrice.multiply(BigInteger.valueOf(GAS_LIMIT_MIN)));
            BigDecimal remainder = token.balance.subtract(new BigDecimal(tx.value).add(networkFee));
            return remainder.equals(BigDecimal.ZERO);
        }

        return false;
    }

    /**
     * Node eth_gasEstimate returned a transaction estimate
     *
     * @param estimate
     */
    public void setGasEstimate(BigInteger estimate)
    {
        //Override custom gas limit
        if (customGasLimit.equals(baseLineGasLimit))
        {
            customGasLimit = estimate;
        }
        if (initialTxGasLimit.equals(BigInteger.ZERO))
        {
            baseLineGasLimit = estimate;
        }

        //presets always use estimate if available
        presetGasLimit = estimate;

        //now update speeds
        handler.post(this);
    }
}


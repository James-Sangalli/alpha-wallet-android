package com.alphawallet.app.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alphawallet.app.C;
import com.alphawallet.app.R;
import com.alphawallet.app.entity.ErrorEnvelope;
import com.alphawallet.app.entity.SignAuthenticationCallback;
import com.alphawallet.app.entity.StandardFunctionInterface;
import com.alphawallet.app.entity.TransactionData;
import com.alphawallet.app.entity.nftassets.NFTAsset;
import com.alphawallet.app.entity.tokens.ERC721Token;
import com.alphawallet.app.entity.tokens.Token;
import com.alphawallet.app.repository.EthereumNetworkBase;
import com.alphawallet.app.router.HomeRouter;
import com.alphawallet.app.service.GasService;
import com.alphawallet.app.ui.widget.OnTokenClickListener;
import com.alphawallet.app.ui.widget.adapter.NonFungibleTokenAdapter;
import com.alphawallet.app.ui.widget.entity.ActionSheetCallback;
import com.alphawallet.app.ui.widget.entity.AddressReadyCallback;
import com.alphawallet.app.ui.zxing.FullScannerFragment;
import com.alphawallet.app.ui.zxing.QRScanningActivity;
import com.alphawallet.app.util.KeyboardUtils;
import com.alphawallet.app.util.QRParser;
import com.alphawallet.app.viewmodel.TransferTicketDetailViewModel;
import com.alphawallet.app.viewmodel.TransferTicketDetailViewModelFactory;
import com.alphawallet.app.web3.entity.Address;
import com.alphawallet.app.web3.entity.Web3Transaction;
import com.alphawallet.app.widget.AWalletAlertDialog;
import com.alphawallet.app.widget.AWalletConfirmationDialog;
import com.alphawallet.app.widget.ActionSheetDialog;
import com.alphawallet.app.widget.FunctionButtonBar;
import com.alphawallet.app.widget.InputAddress;
import com.alphawallet.app.widget.ProgressView;
import com.alphawallet.app.widget.SignTransactionDialog;
import com.alphawallet.app.widget.SystemView;
import com.alphawallet.token.tools.Numeric;

import org.jetbrains.annotations.NotNull;
import org.web3j.protocol.core.methods.response.EthEstimateGas;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.alphawallet.app.C.EXTRA_TOKENID_LIST;
import static com.alphawallet.app.C.GAS_LIMIT_MIN;
import static com.alphawallet.app.C.PRUNE_ACTIVITY;
import static com.alphawallet.app.entity.Operation.SIGN_DATA;
import static com.alphawallet.app.widget.AWalletAlertDialog.ERROR;
import static com.alphawallet.app.widget.AWalletAlertDialog.WARNING;
import static org.web3j.crypto.WalletUtils.isValidAddress;

/**
 * Created by JB on 11/08/2021.
 */
public class TransferNFTActivity extends BaseActivity implements OnTokenClickListener, StandardFunctionInterface, AddressReadyCallback, ActionSheetCallback
{
    @Inject
    protected TransferTicketDetailViewModelFactory viewModelFactory;
    protected TransferTicketDetailViewModel viewModel;
    private SystemView systemView;
    private ProgressView progressView;
    private AWalletAlertDialog dialog;
    private FunctionButtonBar functionBar;

    private Token token;
    private NonFungibleTokenAdapter adapter;
    private ArrayList<NFTAsset> assetSelection;

    private InputAddress addressInput;
    private String sendAddress;
    private String ensAddress;

    private ActionSheetDialog actionDialog;
    private AWalletConfirmationDialog confirmationDialog;

    private LinearLayout pickTicketQuantity;
    private LinearLayout pickTransferMethod;
    private LinearLayout pickExpiryDate;

    private SignAuthenticationCallback signCallback;

    @Nullable
    private Disposable calcGasCost;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_detail);

        token = getIntent().getParcelableExtra(C.EXTRA_TOKEN);

        String tokenIds = getIntent().getStringExtra(EXTRA_TOKENID_LIST);
        assetSelection = getIntent().getParcelableArrayListExtra(C.EXTRA_NFTASSET_LIST);

        toolbar();
        systemView = findViewById(R.id.system_view);
        systemView.hide();
        progressView = findViewById(R.id.progress_view);
        progressView.hide();

        addressInput = findViewById(R.id.input_address);
        addressInput.setAddressCallback(this);

        sendAddress = null;
        ensAddress = null;

        viewModel = new ViewModelProvider(this, viewModelFactory)
                .get(TransferTicketDetailViewModel.class);
        viewModel.progress().observe(this, systemView::showProgress);
        viewModel.queueProgress().observe(this, progressView::updateProgress);
        viewModel.pushToast().observe(this, this::displayToast);
        viewModel.newTransaction().observe(this, this::onTransaction);
        viewModel.error().observe(this, this::onError);
        viewModel.userTransaction().observe(this, this::onUserTransaction);
        viewModel.transactionFinalised().observe(this, this::txWritten);
        viewModel.transactionError().observe(this, this::txError);
        //we should import a token and a list of chosen ids
        RecyclerView list = findViewById(R.id.listTickets);


        //TODO: Build new NFT Asset display module. Needs to have horizonal scroll

        adapter = new NonFungibleTokenAdapter(null, token, selection, viewModel.getAssetDefinitionService());
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);

        pickTicketQuantity = findViewById(R.id.layout_ticket_quantity);
        pickTransferMethod = findViewById(R.id.layout_choose_method);
        pickExpiryDate = findViewById(R.id.layout_date_picker);
        functionBar = findViewById(R.id.layoutButtons);

        functionBar.setupFunctions(this, new ArrayList<>(Collections.singletonList(R.string.action_transfer)));
        functionBar.revealButtons();

        setupScreen();
    }

    private void setupScreen()
    {
        addressInput.setVisibility(View.GONE);
        pickTicketQuantity.setVisibility(View.GONE);
        pickTransferMethod.setVisibility(View.GONE);
        pickExpiryDate.setVisibility(View.GONE);
        addressInput.setVisibility(View.VISIBLE);
        setTitle(R.string.title_input_wallet_address);
    }

    private void onTransaction(String success)
    {
        hideDialog();
        dialog = new AWalletAlertDialog(this);
        dialog.setTitle(R.string.transaction_succeeded);
        dialog.setMessage(success);
        dialog.setIcon(AWalletAlertDialog.SUCCESS);
        dialog.setButtonText(R.string.button_ok);
        dialog.setButtonListener(v -> finish());

        dialog.show();
    }

    private void onUserTransaction(String hash)
    {
        hideDialog();
        dialog = new AWalletAlertDialog(this);
        dialog.setTitle(R.string.transaction_succeeded);
        dialog.setMessage(hash);
        dialog.setButtonText(R.string.copy);
        dialog.setButtonListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("transaction hash",
                    EthereumNetworkBase.getEtherscanURLbyNetworkAndHash(token.tokenInfo.chainId, hash));
            clipboard.setPrimaryClip(clip);
            dialog.dismiss();
            sendBroadcast(new Intent(PRUNE_ACTIVITY));
        });
        dialog.setOnDismissListener(v -> {
            dialog.dismiss();
            sendBroadcast(new Intent(PRUNE_ACTIVITY));
            new HomeRouter().open(this, true);
            finish();
        });
        dialog.show();
    }

    private void hideDialog()
    {
        if (dialog != null && dialog.isShowing())
        {
            dialog.dismiss();
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        viewModel.resetSignDialog();
    }

    private void onProgress(boolean shouldShowProgress)
    {
        hideDialog();
        if (shouldShowProgress)
        {
            dialog = new AWalletAlertDialog(this);
            dialog.setIcon(AWalletAlertDialog.NONE);
            dialog.setTitle(R.string.title_dialog_sending);
            dialog.setMessage(R.string.transfer);
            dialog.setProgressMode();
            dialog.setCancelable(false);
            dialog.show();
        }
    }

    private void onError(@NotNull ErrorEnvelope error)
    {
        hideDialog();
        dialog = new AWalletAlertDialog(this);
        dialog.setIcon(AWalletAlertDialog.ERROR);
        dialog.setTitle(R.string.error_transaction_failed);
        dialog.setMessage(error.message);
        dialog.setCancelable(true);
        dialog.setButtonText(R.string.button_ok);
        dialog.setButtonListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void transferTokenFinal(String to)
    {
        //complete the transfer
        onProgress(true);

        viewModel.createTokenTransfer(
                to,
                token,
                token.getTransferListFormat(selection));
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        viewModel.prepare(token);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        viewModel.stopGasSettingsFetch();
        if (confirmationDialog != null && confirmationDialog.isShowing())
        {
            confirmationDialog.dismiss();
            confirmationDialog = null;
        }
    }

    @Override
    public void onTokenClick(View view, Token token, List<BigInteger> ids, boolean selection) {
        Context context = view.getContext();
        //TODO: what action should be performed when clicking on a range?
    }

    @Override
    public void onLongTokenClick(View view, Token token, List<BigInteger> tokenId)
    {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode >= SignTransactionDialog.REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS && requestCode <= SignTransactionDialog.REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS + 10)
        {
            requestCode = SignTransactionDialog.REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS;
        }

        switch (requestCode)
        {
            case C.BARCODE_READER_REQUEST_CODE:
                switch (resultCode)
                {
                    case FullScannerFragment.SUCCESS:
                        if (data != null)
                        {
                            String barcode = data.getStringExtra(FullScannerFragment.BarcodeObject);

                            //if barcode is still null, ensure we don't GPF
                            if (barcode == null)
                            {
                                Toast.makeText(this, R.string.toast_qr_code_no_address, Toast.LENGTH_SHORT).show();
                                return;
                            }

                            QRParser parser = QRParser.getInstance(EthereumNetworkBase.extraChains());
                            String extracted_address = parser.extractAddressFromQrString(barcode);
                            if (extracted_address == null)
                            {
                                Toast.makeText(this, R.string.toast_qr_code_no_address, Toast.LENGTH_SHORT).show();
                                return;
                            }
                            addressInput.setAddress(extracted_address);
                        }
                        break;
                    case QRScanningActivity.DENY_PERMISSION:
                        showCameraDenied();
                        break;
                    default:
                        Log.e("SEND", String.format(getString(R.string.barcode_error_format),
                                "Code: " + resultCode
                        ));
                        break;
                }
                break;

            case C.SET_GAS_SETTINGS:
                if (data != null && actionDialog != null)
                {
                    int gasSelectionIndex = data.getIntExtra(C.EXTRA_SINGLE_ITEM, -1);
                    long customNonce = data.getLongExtra(C.EXTRA_NONCE, -1);
                    BigDecimal customGasPrice = data.hasExtra(C.EXTRA_GAS_PRICE) ?
                            new BigDecimal(data.getStringExtra(C.EXTRA_GAS_PRICE)) : BigDecimal.ZERO; //may not have set a custom gas price
                    BigDecimal customGasLimit = new BigDecimal(data.getStringExtra(C.EXTRA_GAS_LIMIT));
                    long expectedTxTime = data.getLongExtra(C.EXTRA_AMOUNT, 0);
                    actionDialog.setCurrentGasIndex(gasSelectionIndex, customGasPrice, customGasLimit, expectedTxTime, customNonce);
                }
                break;
            case C.COMPLETED_TRANSACTION:
                Intent i = new Intent();
                i.putExtra(C.EXTRA_TXHASH, data.getStringExtra(C.EXTRA_TXHASH));
                setResult(RESULT_OK, new Intent());
                finish();
                break;
            case SignTransactionDialog.REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS:
                if (actionDialog != null && actionDialog.isShowing()) actionDialog.completeSignRequest(resultCode == RESULT_OK);
                //signCallback.gotAuthorisation(resultCode == RESULT_OK);
                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void showCameraDenied()
    {
        dialog = new AWalletAlertDialog(this);
        dialog.setTitle(R.string.title_dialog_error);
        dialog.setMessage(R.string.error_camera_permission_denied);
        dialog.setIcon(ERROR);
        dialog.setButtonText(R.string.button_ok);
        dialog.setButtonListener(v -> {
            dialog.dismiss();
        });
        dialog.show();
    }

    private void handleERC875Transfer(final String to, final String ensName)
    {
        //how many indices are we selling?
        int quantity = selection.size();
        int ticketName = (quantity > 1) ? R.string.tickets : R.string.ticket;

        String toAddress = (ensName == null) ? to : ensName;

        String qty = String.valueOf(quantity) + " " +
                getResources().getString(ticketName) + "\n" +
                getResources().getString(R.string.to) + " " +
                toAddress;

        signCallback = new SignAuthenticationCallback()
        {
            @Override
            public void gotAuthorisation(boolean gotAuth)
            {
                if (gotAuth) viewModel.completeAuthentication(SIGN_DATA);
                else viewModel.failedAuthentication(SIGN_DATA);

                if (gotAuth) transferTokenFinal(to);
            }

            @Override
            public void cancelAuthentication()
            {
                AWalletAlertDialog dialog = new AWalletAlertDialog(getParent());
                dialog.setIcon(AWalletAlertDialog.NONE);
                dialog.setTitle(R.string.authentication_cancelled);
                dialog.setButtonText(R.string.ok);
                dialog.setButtonListener(v -> {
                    dialog.dismiss();
                });
                dialog.setCancelable(true);
                dialog.show();
            }
        };

        confirmationDialog = new AWalletConfirmationDialog(this);
        confirmationDialog.setTitle(R.string.title_transaction_details);
        confirmationDialog.setSmallText(R.string.confirm_transfer_details);
        confirmationDialog.setMediumText(qty);
        confirmationDialog.setPrimaryButtonText(R.string.transfer_tickets);
        confirmationDialog.setSecondaryButtonText(R.string.dialog_cancel_back);
        confirmationDialog.setPrimaryButtonListener(v1 -> viewModel.getAuthorisation(this, signCallback));
        confirmationDialog.setSecondaryButtonListener(v1 -> confirmationDialog.dismiss());
        confirmationDialog.show();
    }

    ActivityResultLauncher<Intent> transferLinkFinalResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                sendBroadcast(new Intent(PRUNE_ACTIVITY)); //TODO: implement prune via result codes
            });

    @Override
    public void handleClick(String action, int actionId)
    {
        //clicked the next button
        if (actionId == R.string.action_transfer)
        {
            //
        }
    }

    @Override
    public void addressReady(String address, String ensName)
    {
        sendAddress = address;
        ensAddress = ensName;
        //complete the transfer
        if (TextUtils.isEmpty(address) || !isValidAddress(address))
        {
            //show address error
            addressInput.setError(getString(R.string.error_invalid_address));
        }
        else
        {
            calculateTransactionCost();
        }
    }

    @Override
    public void showTransferToken(List<BigInteger> selection)
    {
        KeyboardUtils.hideKeyboard(getCurrentFocus());
        addressInput.getAddress();
    }

    private void calculateTransactionCost()
    {
        if ((calcGasCost != null && !calcGasCost.isDisposed()) ||
                (actionDialog != null && actionDialog.isShowing())) return;

        final String txSendAddress = sendAddress;
        sendAddress = null;

        final byte[] transactionBytes = token.getTransferBytes(txSendAddress, selection);

        calculateEstimateDialog();
        //form payload and calculate tx cost
        calcGasCost = viewModel.calculateGasEstimate(viewModel.getWallet(), transactionBytes, token.tokenInfo.chainId, token.getAddress(), BigDecimal.ZERO)
                .map(this::convertToGasLimit)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(estimate -> checkConfirm(estimate, transactionBytes, token.getAddress(), txSendAddress),
                        error -> handleError(error, transactionBytes, token.getAddress(), txSendAddress));
    }

    private BigInteger convertToGasLimit(EthEstimateGas estimate)
    {
        if (estimate.hasError())
        {
            return BigInteger.ZERO;
        }
        else
        {
            return estimate.getAmountUsed();
        }
    }

    private void handleError(Throwable throwable, final byte[] transactionBytes, final String txSendAddress, final String resolvedAddress)
    {
        Log.w(this.getLocalClassName(), throwable.getMessage());
        checkConfirm(BigInteger.ZERO, transactionBytes, txSendAddress, resolvedAddress);
    }


    private void calculateEstimateDialog()
    {
        if (dialog != null && dialog.isShowing()) dialog.dismiss();
        dialog = new AWalletAlertDialog(this);
        dialog.setTitle(getString(R.string.calc_gas_limit));
        dialog.setIcon(AWalletAlertDialog.NONE);
        dialog.setProgressMode();
        dialog.setCancelable(false);
        dialog.show();
    }

    /**
     * Called to check if we're ready to send user to confirm screen / activity sheet popup
     */
    private void checkConfirm(final BigInteger sendGasLimit, final byte[] transactionBytes, final String txSendAddress, final String resolvedAddress) {

        Web3Transaction w3tx = new Web3Transaction(
                new Address(txSendAddress),
                new Address(token.getAddress()),
                BigInteger.ZERO,
                BigInteger.ZERO,
                sendGasLimit,
                -1,
                Numeric.toHexString(transactionBytes),
                -1);

        if (sendGasLimit.equals(BigInteger.ZERO))
        {
            estimateError(w3tx, transactionBytes, txSendAddress, resolvedAddress);
        }
        else
        {
            if (dialog != null && dialog.isShowing())
            {
                dialog.dismiss();
            }

            actionDialog = new ActionSheetDialog(this, w3tx, token, ensAddress,
                    resolvedAddress, viewModel.getTokenService(), this);
            actionDialog.setCanceledOnTouchOutside(false);
            actionDialog.show();
        }
    }

    /**
     * ActionSheetCallback, comms hooks for the ActionSheetDialog to trigger authentication & send transactions
     *
     * @param callback
     */
    @Override
    public void getAuthorisation(SignAuthenticationCallback callback)
    {
        viewModel.getAuthentication(this, viewModel.getWallet(), callback);
    }

    @Override
    public void sendTransaction(Web3Transaction finalTx)
    {
        viewModel.sendTransaction(finalTx, viewModel.getWallet(), token.tokenInfo.chainId);
    }

    @Override
    public void dismissed(String txHash, long callbackId, boolean actionCompleted)
    {
        //ActionSheet was dismissed
        if (!TextUtils.isEmpty(txHash)) {
            Intent intent = new Intent();
            intent.putExtra("tx_hash", txHash);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    @Override
    public void notifyConfirm(String mode) { viewModel.actionSheetConfirm(mode); }

    private void txWritten(TransactionData transactionData)
    {
        actionDialog.transactionWritten(transactionData.txHash);
    }

    //Transaction failed to be sent
    private void txError(Throwable throwable)
    {
        if (dialog != null && dialog.isShowing()) dialog.dismiss();
        dialog = new AWalletAlertDialog(this);
        dialog.setIcon(ERROR);
        dialog.setTitle(R.string.error_transaction_failed);
        dialog.setMessage(throwable.getMessage());
        dialog.setButtonText(R.string.button_ok);
        dialog.setButtonListener(v -> {
            dialog.dismiss();
        });
        dialog.show();
        actionDialog.dismiss();
    }

    private void estimateError(final Web3Transaction w3tx, final byte[] transactionBytes, final String txSendAddress, final String resolvedAddress)
    {
        if (dialog != null && dialog.isShowing()) dialog.dismiss();
        dialog = new AWalletAlertDialog(this);
        dialog.setIcon(WARNING);
        dialog.setTitle(R.string.confirm_transaction);
        dialog.setMessage(R.string.error_transaction_may_fail);
        dialog.setButtonText(R.string.button_ok);
        dialog.setSecondaryButtonText(R.string.action_cancel);
        dialog.setButtonListener(v -> {
            BigInteger gasEstimate = GasService.getDefaultGasLimit(token, w3tx);
            checkConfirm(gasEstimate, transactionBytes, txSendAddress, resolvedAddress);
        });

        dialog.setSecondaryButtonListener(v -> {
            dialog.dismiss();
        });

        dialog.show();
    }
}

package com.alphawallet.app.ui.widget.holder;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alphawallet.app.C;
import com.alphawallet.app.R;
import com.alphawallet.app.entity.AdapterCallback;
import com.alphawallet.app.entity.EventMeta;
import com.alphawallet.app.entity.Transaction;
import com.alphawallet.app.entity.tokens.Token;
import com.alphawallet.app.interact.FetchTransactionsInteract;
import com.alphawallet.app.repository.EventResult;
import com.alphawallet.app.repository.TokensRealmSource;
import com.alphawallet.app.repository.entity.RealmAuxData;
import com.alphawallet.app.service.AssetDefinitionService;
import com.alphawallet.app.service.TokensService;
import com.alphawallet.app.ui.TokenActivity;
import com.alphawallet.app.ui.TokenFunctionActivity;
import com.alphawallet.app.util.BalanceUtils;
import com.alphawallet.app.util.LocaleUtils;
import com.alphawallet.app.util.Utils;
import com.alphawallet.app.widget.ChainName;
import com.alphawallet.app.widget.TokenIcon;
import com.alphawallet.token.entity.EventDefinition;
import com.alphawallet.token.entity.TSTokenView;
import com.alphawallet.token.tools.Numeric;
import com.alphawallet.token.tools.TokenDefinition;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.util.Date;
import java.util.Map;

import static com.alphawallet.app.C.Key.TICKET;
import static com.alphawallet.app.repository.EthereumNetworkBase.MAINNET_ID;
import static com.alphawallet.app.service.AssetDefinitionService.ASSET_SUMMARY_VIEW_NAME;
import static com.alphawallet.app.ui.widget.holder.TransactionHolder.DEFAULT_ADDRESS_ADDITIONAL;

/**
 * Created by JB on 28/07/2020.
 */
public class EventHolder extends BinderViewHolder<EventMeta> implements View.OnClickListener
{
    public static final int VIEW_TYPE = 2016;

    private final TokenIcon tokenIcon;
    private final TextView date;
    private final TextView type;
    private final TextView address;
    private final TextView value;
    private final ChainName chainName;
    private final TextView supplemental;
    private final LinearLayout transactionBackground;

    private final AssetDefinitionService assetDefinition;
    private final AdapterCallback refreshSignaller;
    private Token token;
    private BigInteger tokenId = BigInteger.ZERO;

    private final FetchTransactionsInteract fetchTransactionsInteract;
    private final TokensService tokensService;
    private String eventKey;
    private boolean fromTokenView;

    public EventHolder(ViewGroup parent, TokensService service, FetchTransactionsInteract interact,
                       AssetDefinitionService svs, AdapterCallback signaller)
    {
        super(R.layout.item_transaction, parent);
        date = findViewById(R.id.text_tx_time);
        tokenIcon = findViewById(R.id.token_icon);
        address = findViewById(R.id.address);
        type = findViewById(R.id.type);
        value = findViewById(R.id.value);
        chainName = findViewById(R.id.chain_name);
        supplemental = findViewById(R.id.supplimental);
        transactionBackground = findViewById(R.id.layout_background);
        tokensService = service;
        itemView.setOnClickListener(this);
        assetDefinition = svs;

        fetchTransactionsInteract = interact;
        refreshSignaller = signaller;
        transactionBackground.setBackgroundResource(R.color.white);
    }

    @Override
    public void bind(@Nullable EventMeta data, @NonNull Bundle addition)
    {
        fromTokenView = false;
        String walletAddress = addition.getString(DEFAULT_ADDRESS_ADDITIONAL);
        //pull event details from DB
        eventKey = TokensRealmSource.eventActivityKey(data.hash, data.eventName);
        tokenId = BigInteger.ZERO;

        RealmAuxData eventData = fetchTransactionsInteract.fetchEvent(walletAddress, eventKey);
        Transaction tx = fetchTransactionsInteract.fetchCached(walletAddress, data.hash);

        if (eventData == null || tx == null)
        {
            // probably caused by a new script detected. Signal to holder we need a reset
            refreshSignaller.resetRequired();
            return;
        }
        token = tokensService.getToken(eventData.getChainId(), eventData.getTokenAddress());

        if (token.getSymbol().equalsIgnoreCase("SART"))
        {
            System.out.println("YOLESS");
        }

        if (token == null) token = tokensService.getToken(data.chainId, walletAddress);
        String sym = token.getShortSymbol();
        tokenIcon.bindData(token, assetDefinition);
        String itemView = null;

        TokenDefinition td = assetDefinition.getAssetDefinition(eventData.getChainId(), eventData.getTokenAddress());
        if (td != null && td.getActivityCards().containsKey(eventData.getFunctionId()))
        {
            TSTokenView view = td.getActivityCards().get(eventData.getFunctionId()).getView(ASSET_SUMMARY_VIEW_NAME);
            if (view != null) itemView = view.tokenView;
        }

        String transactionValue = getEventAmount(eventData, tx);

        if (TextUtils.isEmpty(transactionValue))
        {
            value.setVisibility(View.GONE);
        }
        else
        {
            value.setText(getString(R.string.valueSymbol, transactionValue, sym));
        }

        CharSequence typeValue = Utils.createFormattedValue(getContext(), getTitle(eventData), token);

        type.setText(typeValue);
        //symbol.setText(sym);
        address.setText(eventData.getDetail(getContext(), tx, itemView));// getDetail(eventData, resultMap));
        tokenIcon.setStatusIcon(eventData.getEventStatusType());

        //timestamp
        date.setText(Utils.localiseUnixTime(getContext(), eventData.getResultTime()));
        date.setVisibility(View.VISIBLE);

        if (token.tokenInfo.chainId == MAINNET_ID)
        {
            chainName.setVisibility(View.GONE);
        }
        else
        {
            chainName.setVisibility(View.VISIBLE);
            chainName.setChainID(token.tokenInfo.chainId);
        }
    }

    @Override
    public void setFromTokenView()
    {
        fromTokenView = true;
    }

    private String getEventAmount(RealmAuxData eventData, Transaction tx)
    {
        Map<String, EventResult> resultMap = eventData.getEventResultMap();
        int decimals = token != null ? token.tokenInfo.decimals : C.ETHER_DECIMALS;
        String value = "";
        switch (eventData.getFunctionId())
        {
            case "received":
            case "sent":
                if (resultMap.get("amount") != null)
                {
                    value = eventData.getFunctionId().equals("sent") ? "- " : "+ ";
                    value += BalanceUtils.getScaledValueFixed(new BigDecimal(resultMap.get("amount").value),
                            decimals, 4);
                }
                break;
            case "approvalObtained":
            case "ownerApproved":
                if (resultMap.get("value") != null)
                {
                    value = BalanceUtils.getScaledValueFixed(new BigDecimal(resultMap.get("value").value),
                            decimals, 4);
                }
                break;
            default:
                if (token != null && tx != null)
                {
                    value = token.isEthereum() ? token.getTransactionValue(tx, 4) : tx.getOperationResult(token, 4);
                }
                break;
        }

        return value;
    }

    private String getTitle(RealmAuxData eventData)
    {
        //TODO: pick up item-view
        return eventData.getTitle(getContext());
    }

    private BigInteger getTokenId(TokenDefinition td, RealmAuxData eventData)
    {
        //pull tokenId
        if (token != null && token.isNonFungible() && td != null)
        {
            EventDefinition ev = td.getEventDefinition(eventData.getFunctionId());
            if (ev != null && ev.getFilterTopicValue().equals("tokenId"))
            {
                //filter topic is tokenId, therefore this event refers to a specific tokenId
                //isolate the tokenId
                Map<String, EventResult> resultMap = eventData.getEventResultMap();
                String filterIndexName = ev.getFilterTopicIndex();
                if (resultMap.containsKey(filterIndexName))
                {
                    return new BigInteger(resultMap.get(filterIndexName).value);
                }
            }
        }

        return BigInteger.ZERO;
    }

    @Override
    public void onClick(View view)
    {
        Intent intent = new Intent(getContext(), TokenActivity.class);
        intent.putExtra(C.EXTRA_TOKEN_ID, Numeric.toHexStringNoPrefix(tokenId)); //pass tokenId if event concerns tokenId
        intent.putExtra(C.EXTRA_ACTION_NAME, eventKey);
        intent.putExtra(C.EXTRA_STATE, fromTokenView);
        intent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        getContext().startActivity(intent);
    }
}

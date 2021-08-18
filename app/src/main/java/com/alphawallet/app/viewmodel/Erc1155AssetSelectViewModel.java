package com.alphawallet.app.viewmodel;

import android.content.Context;
import android.content.Intent;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.alphawallet.app.C;
import com.alphawallet.app.entity.Wallet;
import com.alphawallet.app.entity.nftassets.NFTAsset;
import com.alphawallet.app.entity.tokens.Token;
import com.alphawallet.app.interact.FetchTransactionsInteract;
import com.alphawallet.app.service.AssetDefinitionService;
import com.alphawallet.app.service.TokensService;
import com.alphawallet.app.ui.TransferNFTActivity;
import com.alphawallet.app.util.Utils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Erc1155AssetSelectViewModel extends BaseViewModel {
    private final FetchTransactionsInteract fetchTransactionsInteract;
    private final AssetDefinitionService assetDefinitionService;
    private final TokensService tokensService;

    private MutableLiveData<Map<BigInteger, NFTAsset>> assets = new MutableLiveData<>();


    public Erc1155AssetSelectViewModel(FetchTransactionsInteract fetchTransactionsInteract,
                                       AssetDefinitionService assetDefinitionService,
                                       TokensService tokensService)
    {
        this.fetchTransactionsInteract = fetchTransactionsInteract;
        this.assetDefinitionService = assetDefinitionService;
        this.tokensService = tokensService;
    }

    public TokensService getTokensService()
    {
        return tokensService;
    }

    public AssetDefinitionService getAssetDefinitionService()
    {
        return this.assetDefinitionService;
    }

    public LiveData<Map<BigInteger, NFTAsset>> assets() {
        return assets;
    }

    public void getAssets(Token token, BigInteger tokenId)
    {
        if (tokenId.compareTo(BigInteger.ZERO) > 0)
        {
            assets.postValue(token.getTokenAssetMap(tokenId));
        }
        else
        {
            assets.postValue(token.getTokenAssets());
        }
    }

    public void completeTransfer(Context ctx, Token token, ArrayList<NFTAsset> selection, Wallet wallet)
    {
        Intent intent = new Intent(ctx, TransferNFTActivity.class);
        intent.putExtra(C.Key.WALLET, wallet);
        intent.putExtra(C.EXTRA_TOKEN, token);
        intent.putParcelableArrayListExtra(C.EXTRA_NFTASSET_LIST, selection);

        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        ctx.startActivity(intent);
    }
}

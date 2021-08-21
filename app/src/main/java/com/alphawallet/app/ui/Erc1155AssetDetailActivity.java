package com.alphawallet.app.ui;

import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.alphawallet.app.BuildConfig;
import com.alphawallet.app.C;
import com.alphawallet.app.R;
import com.alphawallet.app.entity.StandardFunctionInterface;
import com.alphawallet.app.entity.Wallet;
import com.alphawallet.app.entity.WalletType;
import com.alphawallet.app.entity.nftassets.NFTAsset;
import com.alphawallet.app.entity.tokens.Token;
import com.alphawallet.app.viewmodel.Erc1155AssetDetailViewModel;
import com.alphawallet.app.viewmodel.Erc1155AssetDetailViewModelFactory;
import com.alphawallet.app.widget.FunctionButtonBar;
import com.alphawallet.app.widget.NFTImageView;
import com.alphawallet.app.widget.TokenInfoCategoryView;
import com.alphawallet.app.widget.TokenInfoView;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

public class Erc1155AssetDetailActivity extends BaseActivity implements StandardFunctionInterface {
    @Inject
    Erc1155AssetDetailViewModelFactory viewModelFactory;
    Erc1155AssetDetailViewModel viewModel;

    private Token token;
    private Wallet wallet;
    private BigInteger tokenId;
    private String sequenceId;

    private LinearLayout tokenInfoLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_erc1155_asset_detail);

        toolbar();

        getIntentData();

        setTitle(token.tokenInfo.name);

        initViews();

        initViewModel();

        setupFunctionBar();
    }

    private void initViews()
    {
        tokenInfoLayout = findViewById(R.id.layout_token_info);
        NFTImageView tokenImage = findViewById(R.id.asset_image);
        NFTAsset asset = token.getTokenAssets().get(tokenId);

        if (asset == null) return;

        tokenImage.setupTokenImage(asset);

        TextView tokenDescription = findViewById(R.id.token_description);

        tokenInfoLayout.addView(new TokenInfoCategoryView(this, "Details"));

        if (!TextUtils.isEmpty(sequenceId)) { addInfoView("Sequence #", sequenceId); }
        addInfoView("External Link", asset.getExternalLink());
        tokenInfoLayout.addView(new TokenInfoCategoryView(this, "Description"));
        tokenDescription.setText(asset.getDescription());

        tokenInfoLayout.forceLayout();
    }

    private void getIntentData()
    {
        token = getIntent().getParcelableExtra(C.EXTRA_TOKEN);
        wallet = getIntent().getParcelableExtra(C.Key.WALLET);
        tokenId = new BigInteger(getIntent().getStringExtra(C.EXTRA_TOKEN_ID));
        sequenceId = getIntent().getStringExtra(C.EXTRA_STATE);
    }

    private void initViewModel()
    {
        viewModel = new ViewModelProvider(this, viewModelFactory)
                .get(Erc1155AssetDetailViewModel.class);
    }

    private void setupFunctionBar()
    {
        if (BuildConfig.DEBUG || wallet.type != WalletType.WATCH)
        {
            FunctionButtonBar functionBar = findViewById(R.id.layoutButtons);
            functionBar.setupFunctions(this, viewModel.getAssetDefinitionService(), token, null, Collections.singletonList(tokenId));
            functionBar.revealButtons();
            functionBar.setWalletType(wallet.type);
        }
    }

    private void addInfoView(String elementName, String name)
    {
        if (!TextUtils.isEmpty(name))
        {
            TokenInfoView v = new TokenInfoView(this, elementName);
            v.setValue(name);
            if (name.startsWith("http")) { v.setLink(); }
            tokenInfoLayout.addView(v);
        }
    }

    @Override
    public void showTransferToken(List<BigInteger> selection)
    {
        NFTAsset asset = token.getTokenAssets().get(tokenId);
        viewModel.showTransferToken(this, token, Collections.singletonList(tokenId), new ArrayList<>(Collections.singletonList(asset)));
    }
}

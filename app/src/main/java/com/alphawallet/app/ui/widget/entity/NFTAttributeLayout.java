package com.alphawallet.app.ui.widget.entity;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.gridlayout.widget.GridLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alphawallet.app.R;
import com.alphawallet.app.entity.nftassets.NFTAsset;
import com.alphawallet.app.entity.opensea.Trait;
import com.alphawallet.app.entity.tokens.Token;
import com.alphawallet.app.ui.widget.adapter.TraitsAdapter;
import com.alphawallet.app.widget.TokenInfoCategoryView;

import java.util.List;
import java.util.Map;

/**
 * Created by JB on 2/09/2021.
 */
public class NFTAttributeLayout extends LinearLayout {
    private final FrameLayout layout;
    private final GridLayout grid;
    private final RecyclerView recyclerView;
    private TokenInfoCategoryView labelAttributes;

    public NFTAttributeLayout(Context context, @Nullable AttributeSet attrs)
    {
        super(context, attrs);
        View view = inflate(context, R.layout.item_nft_attributes, this);

        layout = view.findViewById(R.id.layout);
        grid = view.findViewById(R.id.grid);
        recyclerView = view.findViewById(R.id.recycler_view);

        labelAttributes = new TokenInfoCategoryView(context, context.getString(R.string.label_attributes));
        layout.addView(labelAttributes);

        grid.setAlignmentMode(GridLayout.ALIGN_BOUNDS);
        grid.setUseDefaultMargins(false);
    }

    public void bind(Token token, NFTAsset asset)
    {
        Map<String, String> attributes = asset.getAttributes();
        setAttributeLabel(token.tokenInfo.name, attributes.size());
        for (String key : attributes.keySet())
        {
            View attributeView = View.inflate(getContext(), R.layout.item_attribute, null);
            TextView traitType = attributeView.findViewById(R.id.trait);
            TextView traitValue = attributeView.findViewById(R.id.value);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams(
                    GridLayout.spec(GridLayout.UNDEFINED, 1f),
                    GridLayout.spec(GridLayout.UNDEFINED, 1f));
            attributeView.setLayoutParams(params);
            traitType.setText(key);
            traitValue.setText(attributes.get(key));
            grid.addView(attributeView);
        }
    }

    public void bind(Token token, List<Trait> traits)
    {
        TraitsAdapter adapter = new TraitsAdapter(getContext(), traits);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setAdapter(adapter);
        setAttributeLabel(token.tokenInfo.name, adapter.getItemCount());
    }

    private void setAttributeLabel(String tokenName, int size)
    {
        if (size > 0 && tokenName.equalsIgnoreCase("cryptokitties"))
        {
            labelAttributes.setTitle(getContext().getString(R.string.label_cattributes));
            labelAttributes.setVisibility(View.VISIBLE);
        }
        else if (size > 0)
        {
            labelAttributes.setTitle(getContext().getString(R.string.label_attributes));
            labelAttributes.setVisibility(View.VISIBLE);
        }
        else
        {
            labelAttributes.setVisibility(View.GONE);
        }
    }

    //In case anyone is using this and only wants it to remove the gridviews
    @Override
    public void removeAllViews()
    {
        labelAttributes.setVisibility(View.GONE);
        grid.removeAllViews();
    }
}

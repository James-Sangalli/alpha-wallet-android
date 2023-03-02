package com.alphawallet.app.entity;

import android.content.Context;
import android.graphics.drawable.PictureDrawable;
import android.util.Log;

import androidx.annotation.NonNull;

import com.alphawallet.app.util.svg.SvgDecoder;
import com.alphawallet.app.util.svg.SvgDrawableTranscoder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;
import com.caverock.androidsvg.SVG;

import org.jetbrains.annotations.NotNull;

import java.io.InputStream;

/**
 * Created by JB on 24/10/2021.
 */
@GlideModule
public class AlphaWalletGlideModule extends AppGlideModule
{
    @Override
    public void registerComponents(
            @NonNull Context context, @NonNull Glide glide, @NonNull Registry registry)
    {
        registry
                .register(SVG.class, PictureDrawable.class, new SvgDrawableTranscoder())
                .append(InputStream.class, SVG.class, new SvgDecoder());
    }

    // Disable manifest parsing to avoid adding similar modules twice.
    @Override
    public boolean isManifestParsingEnabled()
    {
        return false;
    }

    @Override
    public void applyOptions(@NotNull Context context, GlideBuilder builder) {
        builder.setLogLevel(Log.ERROR);
    }
}

package com.alphawallet.app.repository;

import com.alphawallet.app.entity.lifi.SwapProvider;

import java.util.List;

public interface SwapRepositoryType
{
    public List<SwapProvider> getProviders();
}

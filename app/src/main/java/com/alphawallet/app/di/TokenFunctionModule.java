package com.alphawallet.app.di;

import dagger.Module;
import dagger.Provides;
import com.alphawallet.app.interact.CreateTransactionInteract;
import com.alphawallet.app.interact.FetchTokensInteract;
import com.alphawallet.app.interact.GenericWalletInteract;
import com.alphawallet.app.repository.EthereumNetworkRepositoryType;
import com.alphawallet.app.repository.TokenRepositoryType;
import com.alphawallet.app.repository.TransactionRepositoryType;
import com.alphawallet.app.repository.WalletRepositoryType;
import com.alphawallet.app.router.TransferTicketDetailRouter;
import com.alphawallet.app.service.AssetDefinitionService;
import com.alphawallet.app.service.GasService;
import com.alphawallet.app.service.KeyService;
import com.alphawallet.app.service.OpenseaService;
import com.alphawallet.app.service.TokensService;
import com.alphawallet.app.viewmodel.TokenFunctionViewModelFactory;
/**
 * Created by James on 2/04/2019.
 * Stormbird in Singapore
 */

@Module
public class TokenFunctionModule
{
    @Provides
    TokenFunctionViewModelFactory provideTokenFunctionViewModelFactory(
            AssetDefinitionService assetDefinitionService,
            CreateTransactionInteract createTransactionInteract,
            GasService gasService,
            TokensService tokensService,
            EthereumNetworkRepositoryType ethereumNetworkRepository,
            KeyService keyService,
            GenericWalletInteract genericWalletInteract,
            OpenseaService openseaService,
            FetchTokensInteract fetchTokensInteract) {

        return new TokenFunctionViewModelFactory(
                assetDefinitionService, createTransactionInteract, gasService, tokensService, ethereumNetworkRepository, keyService, genericWalletInteract, openseaService, fetchTokensInteract);
    }

    @Provides
    TransferTicketDetailRouter provideTransferTicketRouter() {
        return new TransferTicketDetailRouter();
    }

    @Provides
    CreateTransactionInteract provideCreateTransactionInteract(TransactionRepositoryType transactionRepository) {
        return new CreateTransactionInteract(transactionRepository);
    }

    @Provides
    FetchTokensInteract provideFetchTokensInteract(TokenRepositoryType tokenRepository) {
        return new FetchTokensInteract(tokenRepository);
    }

    @Provides
    GenericWalletInteract provideGenericWalletInteract(WalletRepositoryType walletRepository) {
        return new GenericWalletInteract(walletRepository);
    }
}

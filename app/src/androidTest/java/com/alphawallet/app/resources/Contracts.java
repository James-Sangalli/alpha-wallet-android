package com.alphawallet.app.resources;

/**
 * Created by JB on 4/09/2022.
 */
public abstract class Contracts
{
    public static String doorContractCode = "0x60806040523480156200001157600080fd5b506040518060400160405280600b81526020016a29aa26102428902237b7b960a91b815250604051806040016040528060068152602001654f464649434560d01b81525081600090816200006691906200042d565b5060016200007582826200042d565b505050620000926200008c620000e460201b60201c565b620000e8565b620000a960076200013a60201b62000e501760201c565b6040518060600160405280603581526020016200218360359139600a90620000d290826200042d565b50620000dd62000143565b5062000521565b3390565b600680546001600160a01b038381166001600160a01b0319831681179093556040519116919082907f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e090600090a35050565b80546001019055565b6006546000906001600160a01b03163314620001a65760405162461bcd60e51b815260206004820181905260248201527f4f776e61626c653a2063616c6c6572206973206e6f7420746865206f776e657260448201526064015b60405180910390fd5b620001bd60076200023860201b62000e591760201c565b90506127108110620002125760405162461bcd60e51b815260206004820152601460248201527f486974207570706572206d696e74206c696d697400000000000000000000000060448201526064016200019d565b6200021e33826200023c565b6200023560076200013a60201b62000e501760201c565b90565b5490565b6001600160a01b038216620002945760405162461bcd60e51b815260206004820181905260248201527f4552433732313a206d696e7420746f20746865207a65726f206164647265737360448201526064016200019d565b6000818152600260205260409020546001600160a01b031615620002fb5760405162461bcd60e51b815260206004820152601c60248201527f4552433732313a20746f6b656e20616c7265616479206d696e7465640000000060448201526064016200019d565b6001600160a01b038216600090815260036020526040812080546001929062000326908490620004f9565b909155505060008181526002602052604080822080546001600160a01b0319166001600160a01b03861690811790915590518392907fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef908290a45050565b505050565b634e487b7160e01b600052604160045260246000fd5b600181811c90821680620003b457607f821691505b602082108103620003d557634e487b7160e01b600052602260045260246000fd5b50919050565b601f8211156200038457600081815260208120601f850160051c81016020861015620004045750805b601f850160051c820191505b81811015620004255782815560010162000410565b505050505050565b81516001600160401b0381111562000449576200044962000389565b62000461816200045a84546200039f565b84620003db565b602080601f831160018114620004995760008415620004805750858301515b600019600386901b1c1916600185901b17855562000425565b600085815260208120601f198616915b82811015620004ca57888601518255948401946001909101908401620004a9565b5085821015620004e95787850151600019600388901b60f8161c191681555b5050505050600190811b01905550565b808201808211156200051b57634e487b7160e01b600052601160045260246000fd5b92915050565b611c5280620005316000396000f3fe60806040526004361061019c5760003560e01c806384c4bd4b116100ec578063a740fc871161008a578063e67876fe11610064578063e67876fe14610452578063e8a3d48514610469578063e985e9c51461047e578063f2fde38b1461049e57600080fd5b8063a740fc87146103fb578063b88d4fde14610412578063c87b56dd1461043257600080fd5b8063985e49f4116100c6578063985e49f4146103a95780639cb8a26a146103be578063a22cb465146103c6578063a49ff5b2146103e657600080fd5b806384c4bd4b1461035f5780638da5cb5b1461037657806395d89b411461039457600080fd5b80634bb309121161015957806370a082311161013357806370a08231146102e7578063715018a6146103155780637b47ec1a1461032a57806382345f991461034a57600080fd5b80634bb30912146102925780636352211e146102a75780636f3bffd2146102c757600080fd5b806301ffc9a7146101a157806306fdde03146101d6578063081812fc146101f8578063095ea7b31461023057806323b872dd1461025257806342842e0e14610272575b600080fd5b3480156101ad57600080fd5b506101c16101bc366004611585565b6104be565b60405190151581526020015b60405180910390f35b3480156101e257600080fd5b506101eb610510565b6040516101cd91906115ef565b34801561020457600080fd5b50610218610213366004611602565b6105a2565b6040516001600160a01b0390911681526020016101cd565b34801561023c57600080fd5b5061025061024b366004611632565b61063c565b005b34801561025e57600080fd5b5061025061026d36600461165c565b610751565b34801561027e57600080fd5b5061025061028d36600461165c565b6107ac565b34801561029e57600080fd5b506101eb6107c7565b3480156102b357600080fd5b506102186102c2366004611602565b6107d6565b3480156102d357600080fd5b506102506102e2366004611724565b61084d565b3480156102f357600080fd5b5061030761030236600461176d565b6108be565b6040519081526020016101cd565b34801561032157600080fd5b50610250610945565b34801561033657600080fd5b50610250610345366004611602565b61097b565b34801561035657600080fd5b50610307610a15565b34801561036b57600080fd5b506007546103079081565b34801561038257600080fd5b506006546001600160a01b0316610218565b3480156103a057600080fd5b506101eb610ac7565b3480156103b557600080fd5b50610307610ad6565b610250610b67565b3480156103d257600080fd5b506102506103e1366004611788565b610b9f565b3480156103f257600080fd5b50610307610bae565b34801561040757600080fd5b506009546103079081565b34801561041e57600080fd5b5061025061042d3660046117c4565b610c0f565b34801561043e57600080fd5b506101eb61044d366004611602565b610c71565b34801561045e57600080fd5b506008546103079081565b34801561047557600080fd5b506101eb610d6a565b34801561048a57600080fd5b506101c1610499366004611840565b610d8a565b3480156104aa57600080fd5b506102506104b936600461176d565b610db8565b60006001600160e01b031982166380ac58cd60e01b14806104ef57506001600160e01b03198216635b5e139f60e01b145b8061050a57506301ffc9a760e01b6001600160e01b03198316145b92915050565b60606000805461051f90611873565b80601f016020809104026020016040519081016040528092919081815260200182805461054b90611873565b80156105985780601f1061056d57610100808354040283529160200191610598565b820191906000526020600020905b81548152906001019060200180831161057b57829003601f168201915b5050505050905090565b6000818152600260205260408120546001600160a01b03166106205760405162461bcd60e51b815260206004820152602c60248201527f4552433732313a20617070726f76656420717565727920666f72206e6f6e657860448201526b34b9ba32b73a103a37b5b2b760a11b60648201526084015b60405180910390fd5b506000908152600460205260409020546001600160a01b031690565b6000610647826107d6565b9050806001600160a01b0316836001600160a01b0316036106b45760405162461bcd60e51b815260206004820152602160248201527f4552433732313a20617070726f76616c20746f2063757272656e74206f776e656044820152603960f91b6064820152608401610617565b336001600160a01b03821614806106d057506106d08133610d8a565b6107425760405162461bcd60e51b815260206004820152603860248201527f4552433732313a20617070726f76652063616c6c6572206973206e6f74206f7760448201527f6e6572206e6f7220617070726f76656420666f7220616c6c00000000000000006064820152608401610617565b61074c8383610e5d565b505050565b6006546001600160a01b0316331461077b5760405162461bcd60e51b8152600401610617906118ad565b6107853382610ecb565b6107a15760405162461bcd60e51b8152600401610617906118e2565b61074c838383610fa2565b61074c83838360405180602001604052806000815250610c0f565b6060600a805461051f90611873565b6000818152600260205260408120546001600160a01b03168061050a5760405162461bcd60e51b815260206004820152602960248201527f4552433732313a206f776e657220717565727920666f72206e6f6e657869737460448201526832b73a103a37b5b2b760b91b6064820152608401610617565b6006546001600160a01b031633146108775760405162461bcd60e51b8152600401610617906118ad565b600a6108838282611981565b507fd6666840ba3b0939cf78131cb173315c425a3385a30b8921494500ca2b49f34a816040516108b391906115ef565b60405180910390a150565b60006001600160a01b0382166109295760405162461bcd60e51b815260206004820152602a60248201527f4552433732313a2062616c616e636520717565727920666f7220746865207a65604482015269726f206164647265737360b01b6064820152608401610617565b506001600160a01b031660009081526003602052604090205490565b6006546001600160a01b0316331461096f5760405162461bcd60e51b8152600401610617906118ad565b610979600061113e565b565b6006546001600160a01b031633146109a55760405162461bcd60e51b8152600401610617906118ad565b6000818152600260205260409020546001600160a01b0316610a095760405162461bcd60e51b815260206004820152601760248201527f6275726e3a206e6f6e6578697374656e7420746f6b656e0000000000000000006044820152606401610617565b610a1281611190565b50565b6006546000906001600160a01b03163314610a425760405162461bcd60e51b8152600401610617906118ad565b612710610a4e60085490565b610a589190611a57565b9050610a676127106002611a6a565b8110610aac5760405162461bcd60e51b8152602060048201526014602482015273121a5d081d5c1c195c881b5a5b9d081b1a5b5a5d60621b6044820152606401610617565b610ab6338261122b565b610ac4600880546001019055565b90565b60606001805461051f90611873565b6006546000906001600160a01b03163314610b035760405162461bcd60e51b8152600401610617906118ad565b506007546127108110610b4f5760405162461bcd60e51b8152602060048201526014602482015273121a5d081d5c1c195c881b5a5b9d081b1a5b5a5d60621b6044820152606401610617565b610b59338261122b565b610ac4600780546001019055565b6006546001600160a01b03163314610b915760405162461bcd60e51b8152600401610617906118ad565b6006546001600160a01b0316ff5b610baa33838361136d565b5050565b6006546000906001600160a01b03163314610bdb5760405162461bcd60e51b8152600401610617906118ad565b610be86127106002611a6a565b600954610bf59190611a57565b9050610c01338261122b565b610ac4600980546001019055565b6006546001600160a01b03163314610c395760405162461bcd60e51b8152600401610617906118ad565b610c433383610ecb565b610c5f5760405162461bcd60e51b8152600401610617906118e2565b610c6b8484848461143b565b50505050565b6000818152600260205260409020546060906001600160a01b0316610cea5760405162461bcd60e51b815260206004820152602960248201527f746f6b656e5552493a2055524920717565727920666f72206e6f6e657869737460448201526832b73a103a37b5b2b760b91b6064820152608401610617565b612710821015610d1357604051806060016040528060358152602001611b496035913992915050565b610d206127106002611a6a565b821015610d4657604051806060016040528060358152602001611b7e6035913992915050565b604051806060016040528060358152602001611bb36035913992915050565b919050565b6060604051806060016040528060358152602001611be860359139905090565b6001600160a01b03918216600090815260056020908152604080832093909416825291909152205460ff1690565b6006546001600160a01b03163314610de25760405162461bcd60e51b8152600401610617906118ad565b6001600160a01b038116610e475760405162461bcd60e51b815260206004820152602660248201527f4f776e61626c653a206e6577206f776e657220697320746865207a65726f206160448201526564647265737360d01b6064820152608401610617565b610a128161113e565b80546001019055565b5490565b600081815260046020526040902080546001600160a01b0319166001600160a01b0384169081179091558190610e92826107d6565b6001600160a01b03167f8c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b92560405160405180910390a45050565b6000818152600260205260408120546001600160a01b0316610f445760405162461bcd60e51b815260206004820152602c60248201527f4552433732313a206f70657261746f7220717565727920666f72206e6f6e657860448201526b34b9ba32b73a103a37b5b2b760a11b6064820152608401610617565b6000610f4f836107d6565b9050806001600160a01b0316846001600160a01b03161480610f765750610f768185610d8a565b80610f9a5750836001600160a01b0316610f8f846105a2565b6001600160a01b0316145b949350505050565b826001600160a01b0316610fb5826107d6565b6001600160a01b0316146110195760405162461bcd60e51b815260206004820152602560248201527f4552433732313a207472616e736665722066726f6d20696e636f72726563742060448201526437bbb732b960d91b6064820152608401610617565b6001600160a01b03821661107b5760405162461bcd60e51b8152602060048201526024808201527f4552433732313a207472616e7366657220746f20746865207a65726f206164646044820152637265737360e01b6064820152608401610617565b611086600082610e5d565b6001600160a01b03831660009081526003602052604081208054600192906110af908490611a89565b90915550506001600160a01b03821660009081526003602052604081208054600192906110dd908490611a57565b909155505060008181526002602052604080822080546001600160a01b0319166001600160a01b0386811691821790925591518493918716917fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef91a4505050565b600680546001600160a01b038381166001600160a01b0319831681179093556040519116919082907f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e090600090a35050565b600061119b826107d6565b90506111a8600083610e5d565b6001600160a01b03811660009081526003602052604081208054600192906111d1908490611a89565b909155505060008281526002602052604080822080546001600160a01b0319169055518391906001600160a01b038416907fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef908390a45050565b6001600160a01b0382166112815760405162461bcd60e51b815260206004820181905260248201527f4552433732313a206d696e7420746f20746865207a65726f20616464726573736044820152606401610617565b6000818152600260205260409020546001600160a01b0316156112e65760405162461bcd60e51b815260206004820152601c60248201527f4552433732313a20746f6b656e20616c7265616479206d696e746564000000006044820152606401610617565b6001600160a01b038216600090815260036020526040812080546001929061130f908490611a57565b909155505060008181526002602052604080822080546001600160a01b0319166001600160a01b03861690811790915590518392907fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef908290a45050565b816001600160a01b0316836001600160a01b0316036113ce5760405162461bcd60e51b815260206004820152601960248201527f4552433732313a20617070726f766520746f2063616c6c6572000000000000006044820152606401610617565b6001600160a01b03838116600081815260056020908152604080832094871680845294825291829020805460ff191686151590811790915591519182527f17307eab39ab6107e8899845ad3d59bd9653f200f220920489ca2b5937696c31910160405180910390a3505050565b611446848484610fa2565b6114528484848461146e565b610c6b5760405162461bcd60e51b815260040161061790611a9c565b60006001600160a01b0384163b1561156457604051630a85bd0160e11b81526001600160a01b0385169063150b7a02906114b2903390899088908890600401611aee565b6020604051808303816000875af19250505080156114ed575060408051601f3d908101601f191682019092526114ea91810190611b2b565b60015b61154a573d80801561151b576040519150601f19603f3d011682016040523d82523d6000602084013e611520565b606091505b5080516000036115425760405162461bcd60e51b815260040161061790611a9c565b805181602001fd5b6001600160e01b031916630a85bd0160e11b149050610f9a565b506001949350505050565b6001600160e01b031981168114610a1257600080fd5b60006020828403121561159757600080fd5b81356115a28161156f565b9392505050565b6000815180845260005b818110156115cf576020818501810151868301820152016115b3565b506000602082860101526020601f19601f83011685010191505092915050565b6020815260006115a260208301846115a9565b60006020828403121561161457600080fd5b5035919050565b80356001600160a01b0381168114610d6557600080fd5b6000806040838503121561164557600080fd5b61164e8361161b565b946020939093013593505050565b60008060006060848603121561167157600080fd5b61167a8461161b565b92506116886020850161161b565b9150604084013590509250925092565b634e487b7160e01b600052604160045260246000fd5b600067ffffffffffffffff808411156116c9576116c9611698565b604051601f8501601f19908116603f011681019082821181831017156116f1576116f1611698565b8160405280935085815286868601111561170a57600080fd5b858560208301376000602087830101525050509392505050565b60006020828403121561173657600080fd5b813567ffffffffffffffff81111561174d57600080fd5b8201601f8101841361175e57600080fd5b610f9a848235602084016116ae565b60006020828403121561177f57600080fd5b6115a28261161b565b6000806040838503121561179b57600080fd5b6117a48361161b565b9150602083013580151581146117b957600080fd5b809150509250929050565b600080600080608085870312156117da57600080fd5b6117e38561161b565b93506117f16020860161161b565b925060408501359150606085013567ffffffffffffffff81111561181457600080fd5b8501601f8101871361182557600080fd5b611834878235602084016116ae565b91505092959194509250565b6000806040838503121561185357600080fd5b61185c8361161b565b915061186a6020840161161b565b90509250929050565b600181811c9082168061188757607f821691505b6020821081036118a757634e487b7160e01b600052602260045260246000fd5b50919050565b6020808252818101527f4f776e61626c653a2063616c6c6572206973206e6f7420746865206f776e6572604082015260600190565b60208082526031908201527f4552433732313a207472616e736665722063616c6c6572206973206e6f74206f6040820152701ddb995c881b9bdc88185c1c1c9bdd9959607a1b606082015260800190565b601f82111561074c57600081815260208120601f850160051c8101602086101561195a5750805b601f850160051c820191505b8181101561197957828155600101611966565b505050505050565b815167ffffffffffffffff81111561199b5761199b611698565b6119af816119a98454611873565b84611933565b602080601f8311600181146119e457600084156119cc5750858301515b600019600386901b1c1916600185901b178555611979565b600085815260208120601f198616915b82811015611a13578886015182559484019460019091019084016119f4565b5085821015611a315787850151600019600388901b60f8161c191681555b5050505050600190811b01905550565b634e487b7160e01b600052601160045260246000fd5b8082018082111561050a5761050a611a41565b6000816000190483118215151615611a8457611a84611a41565b500290565b8181038181111561050a5761050a611a41565b60208082526032908201527f4552433732313a207472616e7366657220746f206e6f6e20455243373231526560408201527131b2b4bb32b91034b6b83632b6b2b73a32b960711b606082015260800190565b6001600160a01b0385811682528416602082015260408101839052608060608201819052600090611b21908301846115a9565b9695505050505050565b600060208284031215611b3d57600080fd5b81516115a28161156f56fe697066733a2f2f516d57393438614e34546a6834654c6b41416f386f733141634d32464a6a413436717461456646416e794e597a59697066733a2f2f516d523331663241556f6b433551794c587a4459556a7935745669626b6a625734766f56754d425a66724e565538697066733a2f2f516d646153546146365758705957694c35636b3763736d5479354557487a595647796b4a5a4e3754523935645353697066733a2f2f516d5567644c7650766a754847664d73754b3148326a467067357231514e63384a655779587952774b5038705466a2646970667358221220e2ef3122a6cb2d5c73f6e9c50a0261a53c008746506d8db79d5ea5e188d01d8a64736f6c63430008100033697066733a2f2f516d58584c464265536a5841774168626f31333434774a536a4c676f557266554b394c4535376f56756261525270";
    public static String usdcContractCode = "0x608060405234801561001057600080fd5b50604051602080610b2983398101806040528101908080519060200190929190505050808060405180807f6f72672e7a657070656c696e6f732e70726f78792e696d706c656d656e74617481526020017f696f6e000000000000000000000000000000000000000000000000000000000081525060230190506040518091039020600019167f7050c9e0f4ca769c69bd3a8ef740bc37934f8e2c036e5a723fd8ee048ed3f8c3600102600019161415156100c657fe5b6100de81610169640100000000026401000000009004565b5060405180807f6f72672e7a657070656c696e6f732e70726f78792e61646d696e000000000000815250601a0190506040518091039020600019167f10d6a54a4754c8869d6886b5f5d7fbfa5b4522237ea5c60d11bc4e7a1ff9390b6001026000191614151561014a57fe5b6101623361024e640100000000026401000000009004565b5050610290565b60006101878261027d6401000000000261084b176401000000009004565b1515610221576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040180806020018281038252603b8152602001807f43616e6e6f742073657420612070726f787920696d706c656d656e746174696f81526020017f6e20746f2061206e6f6e2d636f6e74726163742061646472657373000000000081525060400191505060405180910390fd5b7f7050c9e0f4ca769c69bd3a8ef740bc37934f8e2c036e5a723fd8ee048ed3f8c360010290508181555050565b60007f10d6a54a4754c8869d6886b5f5d7fbfa5b4522237ea5c60d11bc4e7a1ff9390b60010290508181555050565b600080823b905060008111915050919050565b61088a8061029f6000396000f30060806040526004361061006d576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff1680633659cfe6146100775780634f1ef286146100ba5780635c60da1b146101085780638f2839701461015f578063f851a440146101a2575b6100756101f9565b005b34801561008357600080fd5b506100b8600480360381019080803573ffffffffffffffffffffffffffffffffffffffff169060200190929190505050610213565b005b610106600480360381019080803573ffffffffffffffffffffffffffffffffffffffff169060200190929190803590602001908201803590602001919091929391929390505050610268565b005b34801561011457600080fd5b5061011d610308565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b34801561016b57600080fd5b506101a0600480360381019080803573ffffffffffffffffffffffffffffffffffffffff169060200190929190505050610360565b005b3480156101ae57600080fd5b506101b761051e565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b610201610576565b61021161020c610651565b610682565b565b61021b6106a8565b73ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff16141561025c57610257816106d9565b610265565b6102646101f9565b5b50565b6102706106a8565b73ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff1614156102fa576102ac836106d9565b3073ffffffffffffffffffffffffffffffffffffffff163483836040518083838082843782019150509250505060006040518083038185875af19250505015156102f557600080fd5b610303565b6103026101f9565b5b505050565b60006103126106a8565b73ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff1614156103545761034d610651565b905061035d565b61035c6101f9565b5b90565b6103686106a8565b73ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff16141561051257600073ffffffffffffffffffffffffffffffffffffffff168173ffffffffffffffffffffffffffffffffffffffff1614151515610466576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004018080602001828103825260368152602001807f43616e6e6f74206368616e6765207468652061646d696e206f6620612070726f81526020017f787920746f20746865207a65726f20616464726573730000000000000000000081525060400191505060405180910390fd5b7f7e644d79422f17c01e4894b5f4f588d331ebfa28653d42ae832dc59e38c9798f61048f6106a8565b82604051808373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020018273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019250505060405180910390a161050d81610748565b61051b565b61051a6101f9565b5b50565b60006105286106a8565b73ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff16141561056a576105636106a8565b9050610573565b6105726101f9565b5b90565b61057e6106a8565b73ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff1614151515610647576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004018080602001828103825260328152602001807f43616e6e6f742063616c6c2066616c6c6261636b2066756e6374696f6e20667281526020017f6f6d207468652070726f78792061646d696e000000000000000000000000000081525060400191505060405180910390fd5b61064f610777565b565b6000807f7050c9e0f4ca769c69bd3a8ef740bc37934f8e2c036e5a723fd8ee048ed3f8c36001029050805491505090565b3660008037600080366000845af43d6000803e80600081146106a3573d6000f35b3d6000fd5b6000807f10d6a54a4754c8869d6886b5f5d7fbfa5b4522237ea5c60d11bc4e7a1ff9390b6001029050805491505090565b6106e281610779565b7fbc7cd75a20ee27fd9adebab32041f755214dbc6bffa90cc0225b39da2e5c2d3b81604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390a150565b60007f10d6a54a4754c8869d6886b5f5d7fbfa5b4522237ea5c60d11bc4e7a1ff9390b60010290508181555050565b565b60006107848261084b565b151561081e576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040180806020018281038252603b8152602001807f43616e6e6f742073657420612070726f787920696d706c656d656e746174696f81526020017f6e20746f2061206e6f6e2d636f6e74726163742061646472657373000000000081525060400191505060405180910390fd5b7f7050c9e0f4ca769c69bd3a8ef740bc37934f8e2c036e5a723fd8ee048ed3f8c360010290508181555050565b600080823b9050600081119150509190505600a165627a7a72305820a4a547cfc7202c5acaaae74d428e988bc62ad5024eb0165532d3a8f91db4ed2400290000000000000000000000000882477e7895bdc5cea7cb1552ed914ab157fe56";
}

# BBPos_demo

mmt 核心库使用说明：


使用说明：

1、通用刷卡器接口：IPersonalSwiper

参数：
PIN索引：“14”
enum SwipeStatus 状态：成功、失败、取消、超时
enum SwiperType类型：音频、蓝牙
SwipeResult(状态, 卡号, 是否需要输入密码)；返回刷卡结果：
factor 	刷卡过程因子，用于判断是不是一个trans
htDatas	加密方式和加密数据组成的hashtable
SwiperMode flag;
enum SwiperMode 刷卡模式{正常交易，超级交易}

函数：
String getCSN();	获取刷卡器ksn
boolean stopOperation();	停止刷卡器当前操作.
boolean closeDevice();	关闭刷卡器.
SwipeResult swipeCard(String factor, final int timeout);	请求刷卡
String encData(String data, String index, String factor);	使用刷卡器加密数据(null表示加密失败)
Hashtable<String, String> batchEncData(Hashtable<Integer, String> htDatas, String factor);  使用刷卡器批量加密数据(key是明文, value是密文。value为null表示加密失败)
boolean setMode(SwiperMode flag);	选择密钥
Hashtable<String, String> getBankInfo();	返回账户相关信息
boolean isSwiperReady();	判断刷卡器是否可用
SwiperModel getSwiperModel();	获取刷卡器的使用的型号,如p27,M368,wpc2等


2、刷卡器类型(与values/arrays.xml对应)：enum SwiperModel
BBPOS_WISEPAD2_BLUETOOTH(true, false, IPersonalSwiper.SwiperType.BLUETOOTH, true);
SwiperModel(boolean supportPwd, boolean supportPrint, IPersonalSwiper.SwiperType swiperType, boolean supportSwiper)
参数：boolean supportSwiper		返回True  代表蓝牙   false 代表音频

3、刷卡器具体业务接口及回调：
interface IApiSwiper extends IShowSelectApplication, SwiperCallback
实现类：封装业务的刷卡器接口ApiSwiperImplement
请在开始具体业务的时候，务必设置刷卡器的模式

interface IShowSelectApplication {
    void showSelectICApp(ArrayList<String> cardTypes, IICSelectApplicationCallback selectApplicationCallback);	显示用户的卡类型(信用卡、借记卡)
    void showNoAvailableApp(Runnable r);	APP不可用
    void closeSelectICApp();	关闭选择
}
interface IICSelectApplicationCallback {
    void onCardTypeSelect(int index);回调用户选择的卡类型。-1，表示没有任何选择，取消.
}
interface SwiperCallback {
    void onBatteryLow();	低电量
}

4、刷卡器交易接口：interface IBusinessSwiper extends IPersonalSwiper
实现类：需要在具体业务pos类
参数：
position	偏移量
enum Result {确认、超时、取消、错误}

Result affirmAmount(final String amount, final int timeout);	确认金额.当然，如果不支持affirmAmount，也可以通过这个接口设置刷卡金额
Result inputPwd(final String factor, final int timeout);	输入密码
SwipeResult payWithQrCode(String factor, String qrCodeUrl);		二维码支付
SwipeResult registerPwd(final String factor, final int timeout);
boolean printBitMap(int position, Bitmap bitmap);	打印图片
boolean printString(String data);	打印字符
boolean print(String data, Bitmap bitmap);	打印字符和图片
void setPlainPwd(String pwd);	用于设置明文密码。主要用于个人刷卡器

5、设备连接状态接口：interface ISwiperListener extends IShowSelectApplication
实现类：ZZTongActivity，VPOSActivity extends ZZTongActivity

参数：
pan 成功后，是卡明文
isDownlTrd 是否是降级交易
void onBatteryLow();	刷卡器低电量回调
void onShownPwd();	请求界面显示键盘
void onSwiperStatus(int status, int result, String pan, boolean isDownlTrd);
刷卡器状态及结果
void onSwiperStatus(int status, int result, String keyIndex, String encData, String plainData);	刷卡器状态及结果（加密数据）
void onSwiperStatus(int status, int result, HashMap<String, String> hmData);	 刷卡器状态及结果（读取卡信息）
void onSwiperStatus(int status, int result, String encPwd);		刷卡器状态及结果（加密PIN）
long getId();	返回这个listener的唯一标示

6、刷卡器工厂类：ApiSwiperFactory
实现：创建不同实现的ApiSwiper：
参数：
SwiperModel type,刷卡器类型，0表示音频，1表示格尔蓝牙，2，表示新大陆蓝牙。具体参照R.array.swipe_types
string addition	,当type为1的时候，addition表示mac地址

方法：
private static IApiSwiper APISWIPER = new ApiSwiperImplement();获取封装业务的刷卡器接口实例
boolean init(Context c, SwiperModel type, String addition)初始化刷卡器

7、基类ZZTongActivity中定义的状态：
protected byte mAction = Action_INVALID;// 执行协议类型
 public static final byte Action_ReadCSN = 0,// 读取CSN
            Action_Register = 1,// 开通，加密PIN
            Action_GetCSNStatus = 2,// 刷卡器状态查询
            Action_Login = 3,// 使用
            Action_Account = 4,// 账户管理
            Action_AddAccount = 5,// 绑卡，加密PIN
            Action_DelAccount = 6,// 删卡
            Action_QueryBills = 7,// 交易查询
            Action_Transfer1 = 8,// 转账1（选卡确认金额）
            Action_Transfer2 = 9,// 转账2（发起转账请求）
            Action_BalanceEnquiry = 10,// 余额查询
            Action_ModifyPassword1 = 11,// 修改密码1（修改密码界面）
            Action_ModifyPassword2 = 12,// 修改密码2（发起修改密码请求）
            Action_SameTransfer1 = 13,// 同名转账1（选卡确认金额）
            Action_SameTransfer2 = 14,// 同名转账2（发起转账请求）
            Action_SuperTransfer = 15,// 超级转账
            Action_CreditCardRepay = 16,// 信用卡还款
            Action_Phone = 17,// 手机网页充值
            Action_Qb = 18,// Q币网页充值
            Action_Game = 19,// 游戏网页
            Action_MARKET = 20, //  分销系统
            Action_SuperUniTrans = 21,//银联转账
            Action_INVALID = -1,
            Action_QrCodePay=22; // 非法action
    public static final byte Action_FROM_UNI_TRANS = 1, Action_FROM_CREDIT = 2; // 信用卡和银联交易


刷卡器接入：BbposM368
API版本：wisepadapi-android-2.6.1.jar
class BbposM368 extends AbstractBbposSwiper implements WisePadControllerListener, IBusinessSwiper
AbstractBbposSwiper：	Bbpos的抽象方法，主要写一些bbpos组装apdu的方法
abstract class AbstractBbposSwiper implements IPersonalSwiper

参数：
int SINGLE_ENC_DATA_KEY = 0;	单密钥加密，固定用的KEY；用于“13”、“14”
int SINGLE_ENC_MAC_KEY = 255;	单密钥加密，固定用的KEY；用于“15”

步骤：
1.连接蓝牙设备 ：ConnectSwiperActivity
刷卡器连接插件、包含了刷卡器选择、蓝牙选择、音频和蓝牙连接动画界面
类型：默认1，选择刷卡器(0),蓝牙选择设备(-1),音频连接动画界面(-2),蓝牙连接动画界面(-3),自动连接上一次连接过的刷卡器(-4)
(1)选择刷卡器(0)SWIPER_SELECT:
获取设备名称：M368(B0101)
刷卡器模式models：[I@431af7d0swiperModel : ME368_BLUETOOTHposition : 6
记住上一次选择的设备：selectType（音频、蓝牙），selectIndex(固定列表中的position)
onSelectSwiper(),列表选择：
首先，清除上一次选择的数据cleanSwiperData()，mac、csn、刷卡器状态置空，
boolean clearSwiper(IPersonalSwiper.SwiperType swiperType);
(当swiperType为null。肯定清除。否则，只有当前刷卡器的type与传进来的type一致时，才清除。)
调用通用接口boolean closeDevice();每类设备关闭方式不同，需要在SwiperModel具体实现。
其次，判断音频、蓝牙设备：
SwiperModel：目前接入的音频设备类型有
switch (swiperModel) {
            case KOAL_VOICE:
            case GJACK_VOICE_MAG:
            case GJACK_VOICE_IC:
            case BBPOS_VOICE:
            case CMSET_VOICE:
            case NEWLAND_VOICE_IC: {
                return true;
            }
            default:
                return false;
        }
如果选择的是音频设备：
选择后跳转：SWIPER_VOICE_ANIMATION，音频连接动画界面(-2),
连接动画界面：BindDeviceVoiceLayoutView -->> BindDeviceVoiceView
刷卡器开始连接： startSwiperConnect(null)：
	初始化：
	clearSwiper，
	初始化设备new BbPosWisePad2(c.getApplicationContext(), APISWIPER)：
		获取设备控制器，连接设备，初始化线程锁，选择设备(回调)
		设置业务的具体刷卡器void setSwiper(IBusinessSwiper swiper);
		（选择设备：需要确定了支付的卡类型，且卡名称(信用卡、借记卡)可用后，
		在刷卡时SwipeCard.class，选择卡类型onCardTypeSelect()回调showSelectICApp()
		在bbposM368回调onRequestSelectApplication(final ArrayList<String> strings)中
		调用showSelectICApp()，响应onCardTypeSelect()方法
		并设置选择app:wisePadController.selectApplication(index);
		一次交易中，如果用户已经选择了卡类型，直接选择）
如果设备可用，设置业务的具体刷卡器，保存用户选择连接的蓝牙设备Mac:saveConnectedBTMac()。-->>保存BT_MAC_KEY="bluetooth_mac"


刷卡器连接状态监听：
onSwiperStatus(ISwiperListener.Status_Connect, //连接
                    ISwiperListener.Swiper_Plug_In,//插入
                    null, false);
	刷卡状态：
	刷卡器连接(Swiper_Plug_In)，刷卡器断开连接(Swiper_Plug_Off)，
	刷卡成功(Swiper_OK),刷卡超时(Swiper_Timeout),刷卡错误(Swiper_Error),
	取消刷卡(Swiper_Cancel)

	1.Swiper_Plug_In:刷卡器链接
	设置音频口状态打开，获取csn：getCSN(),
	如果csn为空：提示设备认证中……，-->>连接设备doDeviceConnect()
	等待连接ISwiperListener.Status_Connect，发送连接信息dealConnectMessage()
	监听连接状态回调：private Hashtable<Long, ISwiperListener> mCallbacks;
	    如果连接器不可用，回调onSwiperStatus()刷卡错误(Swiper_Error),否则
	    getcsn(),最多尝试5次，如果获取不到csn,回调onSwiperStatus()刷卡错误(Swiper_Error)
	    如果获取到csn，提示连接成功，回调onSwiperStatus()刷卡成功(Swiper_OK)；

	2.Swiper_Plug_Off:刷卡器断开链接
	清除上一次选择的数据cleanSwiperData(),
	onSwiperPlugOff();如果csn不为空，提示刷卡器连接中断
	                  如果csn为空，提示刷卡器连接失败，请重新插入

	3.Swiper_OK:刷卡成功
	getCSN(),判断给定csn是不是新版刷卡器,老刷卡器不支持isNewSwiper(String csn)
	保存csn,onConnectSuccess()为空函数
	4.Swiper_Error：刷卡错误
	清空csn,删除上一次选择的设备，提示刷卡器器连接失败，请重新插入

如果选择的是蓝牙设备：
获取蓝牙过滤器，跳转蓝牙搜索界面SwiperBluetoothSelectView，
选择显示的蓝牙设备，跳转蓝牙连接动画界面SWIPER_BLUETOOTH_ANIMATION
同上：startSwiperConnect()

如果设备不可用，clearSwiper(),清除保存的上一个设备信息，提示：蓝牙设备连接失败

刷卡支付(doOKClickBankCardPay)：

1.请求接口zztGetLowestAccountTransferAmount.do获取最低交易金额
{platform=ANDROID, channel=FASTBILL, version=2.5.2}
返回{"wechatAmount": 2,"amount": 1,"scanpayAmount": 2,"wechatScanpayAmount": 2,"noCardPayAmount": 10000,"alipayAmount": 2}

2. 查询身份证过期接口：queryCardExpiryDate()-->>zztQueryCertNoExpiryDate
requestBody:{account=F7B6D3451102E8E06E6D8124ABC1BC95}
status=01/03身份证过期，去提交。-1无相关记录(标识身份证可用)
 {"remindMessage": "","status": "-1"}

3.获取可用支付方式：getTradeType()请求接口getAvailableTradeType.do//获取支付方式
requestBody:{channel=FASTBILL, clientChannel=FASTBILL}
 {"items": 6,"list": [{"tradeType": 5,"isOpen": 1},{"tradeType": 1,"isOpen": 1}]}
新版扫描码支付可用：open == 1 && type == 1
打开刷卡支付：isOpenBank = true；金额置零restZero()

刷卡支付：

	绑卡：doOKClickBankCardPay()
	如果没有上传资质：AccountUtils.AccountStatus.NOUPLOAD
	 检查刷卡器状态：mainActivity.checkVPOS(mainActivity, true)
		未连接蓝牙设备，提示：请连接刷卡器，跳转连接页面ConnectSwiperActivity
		设置SWIPER_STATE为自动连接
		已连接:
		强制打开gps,提示：请打开地理位置服务，
		如果已打开gps，获取地址位置：application.requestLocation()
		如果有地址信息，且需要查询刷卡器状态(是否可绑定及绑定关系)，请求接口："zztGetSwiperStatus.do"
		参数：(加密csn,address,account,channel)
			设置刷卡器状态
			判断当前刷卡器是不是绑定当前账户下的isAccountSwiper()，
			是，返回0，-->>goToVPos()，mLastClickTime = 0L;(等待其他操作)
			否，判断是否可以绑定，是-->>goToVPos()，否则提示服务端返回字段
		若未上传资质材料，
			只有当该刷卡器，不是当前账户的，且处于可以绑定的状态下，才通过检测。
			否则提示当前业务状态。
		若已上传资质，
			如果未绑定当前账户，
				可以绑定，提示：是否绑定该刷卡器？如果csn不为空，  requestBindSwiper(csn);调用绑定刷卡器接口zztBindSwiper.do，绑定用户手机号。
				不可绑定，提示后台返回其他状态
		进入提交资质审核界面。
	如果已上传资质，	比较用户地址是否在限定的地址内，东西经，南北纬处理，只在国内可以不处理(假设都是北半球，南半球只有澳洲具有应用意义) ，
	如果通过地址限定，调用费率列表接口zztRateList.do

	选择费率，设置刷卡器模式：
	RateDialogFragment，选择费率后，代理点击事件onOkClick(Dialog dialog, Rate obj)，如果金额>0,且未超过最高限额，-->>onVPOS()-->>设置刷卡器模式;
	保存刷卡器可用状态BandSwiperStatus(可用为false)
	设置刷卡器模式(正常交易)，setMode(SwiperMode, successTask)
		若未提交资质，判断pos状态checkVPOS()，同绑卡流程
		检测刷卡器状态，checkCsn()，用户名是否可用，资质及判断pos状态		异步执行通用刷卡器接口的选择密钥方法：setmode(SwiperMode),(setMode是个耗时的操作.例如.所以移到非工作线程里做)，不同刷卡器的设置模式可能不同，需具体实现。
		只有在下单成功的情况下,才视为交易,在这里加一个消费通知的标记,方便后面判断使用ClientEngine.getInstance().saveGlobal(IBusinessSwiper.BANK_TransactionType_KEY, "PurchaseAdvice");
		ApiSwiperFactory.getInstance().setMode(m)，
		模式设置完毕回到主线程执行goTo_VPOS_Activity()；

	刷卡流程：goTo_VPOS_Activity()
	检查刷卡器状态checkVPOS()，按照地址判断交易范围，跳转刷卡界面VPOSActivity
	“消费持卡人实名认证”，
	用于每次一个交易完成，清除加密的过程因子clearFactor()

	刷卡状态的接口：

	超级转账：
	Action_SuperTransfer = 15,// 超级转账
    Action_CreditCardRepay = 16,// 信用卡还款
	Action_SuperUniTrans = 21,//银联转账
	需要转入卡：Transfer_To_Accounts_Key(账户管理中选中的账户key)

	Action_Phone = 17,// 手机网页充值
	Action_Qb = 18,// Q币网页充值
	Action_Game = 19,// 游戏网页

	Action_BalanceEnquiry = 10,// 余额查询
	调用：doSwipe();

	Action_QrCodePay=22; // 二维码支付
	调用payWithQrCode(qrCodeData)；

刷卡交易流程：确认金额-刷卡-输密码

	如果支持确认金额：金额长度10；
	 void doAffirmAmount(String amount, long listenId)
		-->> dealAffirmAmountMessage(msg.obj.toString());
			如果不支持确认金额。就不存在回调函数。直接返回
		-->>等待确认金额：Status_AffirmAmount
			调用交易接口IBusinessSwiper的确认金额.当然，如果不支持affirmAmount，也可以通过这个接口设置刷卡金额Result affirmAmount(final String amount, final int timeout)在具体pos中实现，
			回调等待确认金额：ISwiperListener.Status_AffirmAmount
				回调成功后，判断刷卡器模式，
				如果是正常交易doSwipe(mCallbackId);，跳转刷卡ISwiperListener.Status_Swipe，
					等待刷卡：
					初始化时间：initFactor()，
					请求刷卡：dealSwipeMessage()-->>swipeCard(String factor, final int timeout)，在具体业务类中实现；
						swipeCard()回调刷卡结果(即onSwiperStatus中的status)：
						1.SWIPE_OK
						新版刷卡器 swipeStatus返回了银行卡卡号 !这也算是刷卡成功！
						返回结果：存入"PlainAccountKey"，如果卡类型为“2”，“6”，该卡可能存在ic卡，提示客户改用ic卡操作
						如果是正常交易或者超级交易，调用刷卡器状态接口ISwiperListener的
						onSwiperStatus(int status, int result, String pan, boolean isDownlTrd)方法
						回调实现类为刷卡流程VPOSActivity.class,
						is.onSwiperStatus(ISwiperListener.Status_Swipe, ISwiperListener.Swiper_OK, result.mData, isDownloadTrd)
						如果结果为ISwiperListener.Swiper_OK，判断是否可以降级交易
                            如果不可以降级交易DW_TRADE，提示“该卡为ic磁条卡，请插入ic卡插槽进行交易”，点击后重新刷卡-->> ApiSwiperFactory.getInstance().doSwipe(getId())
                            如果可以降级,判断基类中的执行协议类型mAction：
                                (刷卡交易的执行类型)
                                case Action_Register:// 开通，加密PIN
                                case Action_AddAccount:// 绑卡，加密PIN
                                    判断刷卡器状态：ISwiperListener
                                    .Status_EncyptData-->>.Status_Swipe
                                    Status_Swipe-->>Status_InputPwdRegister
                                    Status_InputPwdRegister-->>Status_MAC
                                case Action_Transfer1:// 转账1（选卡确认金额）
                                case Action_Transfer2:// 转账2（发起转账请求）
                                case Action_SameTransfer1:// 同名转账1（选卡确认金额）
                                case Action_SameTransfer2:// 同名转账2（发起转账请求）
                                case Action_MARKET:// 分销系统
                                    判断刷卡器状态：ISwiperListener
                                    .Status_AffirmAmount-->>Status_Swipe 如果转出卡明文mPlainAccount为空，提示“刷卡或插卡异常，请再试一次”-->>doSwipe();
                                    .Status_InputPwdRegister-->>Status_MAC
                                case Action_QrCodePay: 二维码，
                                    扫码后直接退出
                                case Action_BalanceEnquiry:// 余额查询
                                    判断刷卡器状态：ISwiperListener
                                    Status_Swipe-->>showPwd()-->>等待输入密码：Status_InputPwdTrade
                                    设置明文密码。用户用户从UI上设置完密码后，回调 ApiSwiperFactory.getInstance().doSetPwd(pwd, activity.getId());
                                    "pwd"="0"标识用户取消
                                        若已存在bankInfo，-->>dealGetBankInfoMessage()									获取账户信息：getBankInfo()；									如果是新增的消费通知，那么无论是IC或者磁条,都需要做mac,一切的一切都是为了得到mac，要控制好transationType这个标记,否则很容易走错通道,就会挂掉。									将MerchantID,TerminalID,OrderID,TransAmount,BANK_ACCOUNT,BANK_TRACK2，ICNUM(可以为空)，DCDATA存入linkedHashMap,据此获取到mac明文，使用刷卡器加密数据encData(mac, ISwiperListener.MacDataIndex, mFactor)，其中，第二个参数为交易时交换apdu的“13”，“14”，“15”号cmd命令编号。
                                        如果是ic卡，需要做mac，获取方式同上。
                                        获取mac成功后，回调刷卡成功ISwiperListener.Swiper_OK

                                        若不存在bankInfo，
                                            本地存在密码“pwd”-->>dealInputPwdMessage();
                                            "pwd"==0 -->>dealInputPwdResult(Result.CANCEL);
                                            "pwd"长度为6的数字-->>在具体业务类中实现
                                            -->>设置明文密码mSwiper.setPlainPwd(pwd);；
                                            -->>输入密码Result inputPwd(final String factor, final int timeout);
                                            -->>交易密码结果dealInputPwdResult(result);	若结果为确认：doGetBankInfo(mCallbackId);-->>ISwiperListener.Status_InputPwdTrade,同上showPwd()；

                                    Status_InputPwdTrade-->> Status_MAC

                                case Action_Phone:
                                case Action_Qb:
                                case Action_Game:
                                    判断操作状态：ISwiperListener
                                    Status_Swipe-->> showPwd();
                                    Status_InputPwdTrade-->> 加密数据Status_EncyptData
                                    dealEncryptDataMessage(msg.obj.toString(), ISwiperListener.KeyDataIndex);-->>
                                        (计算mac：dealEncryptDataMessage()) mSwiper.encData(plainData, keyIndex, mFactor)
                                            判断操作状态：ISwiperListener
                                            1.通知用户加密后的数据：Status_InputPwdRegister：-->>等待输入密码绑卡doRegisterPwdProcess2()
                                            2.计算mac,或加密数据Status_MAC或Status_EncyptData：
                                case Action_SuperTransfer:// 超级转账
                                case Action_CreditCardRepay: { 信用卡还款
                                    判断状态：
                                    如果是刷卡-->>调用接口查询快速转账手续费：Do_SubatmQueryFee	如果是支付金额 -->>showPwd();
                                case Action_SuperUniTrans:	银联转账	
                                    判断状态：
                                    如果是刷卡-->>affirmAmount();
                                    如果是支付金额 -->>showPwd();

                            2.SWIPE_CANCEL-->>退出
                            3.SWIPE_ERROR & 4.SWIPE_TIMEOUT
                                如果未连接或获取csn失败-->>退出
                                如果是正在刷卡、确认金额、输入密码，提示异常或超时，重发消息
			如果是超级交易doInputPwd(mCallbackId)，跳转等待输入密码交易ISwiperListener.Status_InputPwdTrade，
				回调刷卡器状态及结果onSwiperStatus()

	其他状态监听：
		刷卡器拔出：onSwiperPlugOff()
			清除消费通知标识："transationType"，clearFactor();
			调用停止刷卡器：stopAdpu();-->> 异步执行刷卡器具体业务：ApiSwiperFactory.getInstance().stopOperation();
			如果是超级转账或者查询余额，直接返回到功能主界面
			如果是刷卡转账，直接返回到输入金额界面
		无卡支付页面回调：四要素验证和银行卡识别拍照Status_Four_Elements
		银联转账：Action_SuperUniTrans -->> Status_Four_Elements
			打开对话框：CameraCardPhotoActivity

































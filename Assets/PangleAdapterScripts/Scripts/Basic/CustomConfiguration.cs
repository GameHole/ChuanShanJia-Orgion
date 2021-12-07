
namespace ByteDance.Union
{
    public class CustomConfiguration
    {
        /// <summary>
        ///是否允许SDK主动使用ACCESS_WIFI_STATE权限
        /// </summary>
        /// <returns> true可以使用，false禁止使用。默认为true</returns>
        public bool CanUseWifiState { get; set; } = true;

        /// <summary>
        /// 当isCanUseWifiState=false时，可传入Mac地址信息，穿山甲sdk使用您传入的Mac地址信息
        /// </summary>
        /// <returns>Mac地址信息</returns>
        public string MacAddress { get; set; } = null;
    }
}
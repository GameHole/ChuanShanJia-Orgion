namespace ByteDance.Union {
#if !UNITY_EDITOR && UNITY_ANDROID
    using System;
    using UnityEngine;

    /// <summary>
    ///manager for native ad and express ad.
    /// </summary>
    public class NativeAdManager {

        protected readonly AndroidJavaObject nativeAdManager;
        private static NativeAdManager sNativeAdManager = new NativeAdManager();

        /// <summary>
        /// Initializes a new instance of the <see cref="NativeAd"/> class.
        /// </summary>
        private NativeAdManager()
        {
            var jc = new AndroidJavaClass(
                       "com.bytedance.android.NativeAdManager");
            AndroidJavaObject manager = jc.CallStatic<AndroidJavaObject>("getNativeAdManager");
            this.nativeAdManager = manager;
        }
        public static NativeAdManager Instance()
        {
            return sNativeAdManager;
        }
        /// <summary>
        /// Shows the express ad.
        /// </summary>
        /// <param name="expressAd">Express ad.</param>
        /// <param name="listener">Listener.</param>
        /// <param name="dislikeInteractionListener">Dislike interaction listener.</param>
        public void ShowExpressFeedAd(AndroidJavaObject activity, AndroidJavaObject expressAd,IExpressAdInteractionListener listener,IDislikeInteractionListener dislikeInteractionListener)
        {
            // this.nativeAdManager.Call("showExpressFeedAd", activity, expressAd,
            //                           new ExpressAdInteractionCallback(listener),
            //                           new DisLikeCallback(dislikeInteractionListener));
            object[] objs = {activity, expressAd,new ExpressAdInteractionCallback(listener),new DisLikeCallback(dislikeInteractionListener)};
            var signature = "(Landroid.content.Context;Lcom.bytedance.sdk.openadsdk.TTNativeExpressAd;Lcom.bytedance.sdk.openadsdk.TTNativeExpressAd$AdInteractionListener;Lcom.bytedance.sdk.openadsdk.TTAdDislike$DislikeInteractionCallback;)V";
            CallJavaMethod("showExpressFeedAd",signature, objs);
        }

        private void CallJavaMethod(string methodName,string signature, object[] objs)
        {
            var methodID =
                AndroidJNIHelper.GetMethodID(nativeAdManager.GetRawClass(), methodName, signature);
            var jniArgArray = AndroidJNIHelper.CreateJNIArgArray(objs);
            try
            {
                AndroidJNI.CallVoidMethod(nativeAdManager.GetRawObject(), methodID, jniArgArray);
            }
            finally
            {
                AndroidJNIHelper.DeleteJNIArgArray(objs, jniArgArray);
            }
        }

        /// <summary>
        /// Shows the express banner ad.
        /// </summary>
        /// <param name="expressAd">Express ad.</param>
        /// <param name="listener">Listener.</param>
        /// <param name="dislikeInteractionListener">Dislike interaction listener.</param>
        public void ShowExpressBannerAd(AndroidJavaObject activity, AndroidJavaObject expressAd,IExpressAdInteractionListener listener,IDislikeInteractionListener dislikeInteractionListener)
        { 
            // this.nativeAdManager.Call("showExpressBannerAd", activity, expressAd,
            //                           new ExpressAdInteractionCallback(listener),
            //                           new DisLikeCallback(dislikeInteractionListener));
            object[] objs = {activity, expressAd,new ExpressAdInteractionCallback(listener),new DisLikeCallback(dislikeInteractionListener)};
            var signature = "(Landroid.content.Context;Lcom.bytedance.sdk.openadsdk.TTNativeExpressAd;Lcom.bytedance.sdk.openadsdk.TTNativeExpressAd$AdInteractionListener;Lcom.bytedance.sdk.openadsdk.TTAdDislike$DislikeInteractionCallback;)V";
            CallJavaMethod("showExpressBannerAd",signature, objs);

        }

        /// <summary>
       /// Shows the express interstitial ad.
       /// </summary>
        /// <param name="expressAd">Express ad.</param>
        /// <param name="listener">Listener.</param>
        public void ShowExpressInterstitialAd(AndroidJavaObject activity, AndroidJavaObject expressAd,
            IExpressAdInteractionListener listener)
        {
            // this.nativeAdManager.Call("showExpressIntersititalAd", activity, expressAd,
            //                          new ExpressAdInteractionCallback(listener));
            var signature =
                "(Landroid.content.Context;Lcom.bytedance.sdk.openadsdk.TTNativeExpressAd;Lcom.bytedance.sdk.openadsdk.TTNativeExpressAd$AdInteractionListener;)V";
            object[] objs = {activity, expressAd, new ExpressAdInteractionCallback(listener)};
            CallJavaMethod("showExpressIntersititalAd",signature, objs);
        }

        /// <summary>
        /// Destories the express ad.
        /// </summary>
        /// <param name="expressAd">Express ad.</param>
        public void DestoryExpressAd(AndroidJavaObject expressAd) {
            this.nativeAdManager.Call("destoryExpressAd", expressAd);
        }


        private sealed class ExpressAdInteractionCallback : AndroidJavaProxy
        {
            private IExpressAdInteractionListener listener;
            public ExpressAdInteractionCallback(IExpressAdInteractionListener callback) : base("com.bytedance.sdk.openadsdk.TTNativeExpressAd$AdInteractionListener")
            {
                this.listener = callback;
            }

            void onAdDismiss()
            {
                UnityDispatcher.PostTask(
                    () => this.listener.OnAdClose(null));
            }

            void onAdClicked(AndroidJavaObject view, int type)
            {
                UnityDispatcher.PostTask(
                   () => this.listener.OnAdClicked(null));
            }


            void onAdShow(AndroidJavaObject view, int type)
            {
                UnityDispatcher.PostTask(
                   () => this.listener.OnAdShow(null));
            }


            void onRenderFail(AndroidJavaObject view, string msg, int code)
            {
                UnityDispatcher.PostTask(
                   () => this.listener.OnAdViewRenderError(null, code, msg));
            }


            void onRenderSuccess(AndroidJavaObject view, float width, float height)
            {
                UnityDispatcher.PostTask(
                    () => listener.OnAdViewRenderSucc(null, width, height));
            }
        }

        private sealed class DisLikeCallback : AndroidJavaProxy
        {
            private IDislikeInteractionListener dislikeInteractionCallback;
            public DisLikeCallback(IDislikeInteractionListener dislike) : base("com.bytedance.sdk.openadsdk.TTAdDislike$DislikeInteractionCallback")
            {
                this.dislikeInteractionCallback = dislike;
            }

            private void onSelected(int position, string value, bool enforce)
            {
                Debug.Log("DisLikeCallback -->onSelected position -" + position + " value---" + value);
                UnityDispatcher.PostTask(
             () => this.dislikeInteractionCallback.OnSelected(position, value, enforce));
            }


            private void onCancel()
            {
                UnityDispatcher.PostTask(
             () => this.dislikeInteractionCallback.OnCancel());
            }
            public void onShow()
            {
                UnityDispatcher.PostTask(
                    () => this.dislikeInteractionCallback.OnShow());
            }
        }

    }
#endif
}

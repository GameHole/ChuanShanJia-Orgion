//------------------------------------------------------------------------------
// Copyright (c) 2018-2019 Beijing Bytedance Technology Co., Ltd.
// All Right Reserved.
// Unauthorized copying of this file, via any medium is strictly prohibited.
// Proprietary and confidential.
//------------------------------------------------------------------------------

namespace ByteDance.Union
{
#if (DEV || !UNITY_EDITOR) && UNITY_ANDROID
    using System;
    using UnityEngine;
    using System.Collections.Generic;

    /// <summary>
    /// The reward video Ad.
    /// </summary>
    public sealed class RewardVideoAd : IDisposable, IClintBidding
    {
        private readonly AndroidJavaObject ad;

        /// <summary>
        /// Initializes a new instance of the <see cref="RewardVideoAd"/> class.
        /// </summary>
        internal RewardVideoAd(AndroidJavaObject ad)
        {
            this.ad = ad;
        }

        /// <inheritdoc/>
        public void Dispose()
        {
        }

        /// <summary>
        /// Sets the interaction listener for this Ad.
        /// </summary>
        public void SetRewardAdInteractionListener(
            IRewardAdInteractionListener listener)
        {
            var androidListener = new RewardAdInteractionListener(listener);
            this.ad.Call("setRewardAdInteractionListener", androidListener);
        }

        /// <summary>
        ///  注册激励视频再看一个广告回调
        /// </summary>
        public void SetAgainRewardAdInteractionListener(
            IRewardAdInteractionListener againListener)
        {
            var androidListener = new RewardAdInteractionListener(againListener);
            this.ad.Call("setRewardPlayAgainInteractionListener", androidListener);
        }

        /// <summary>
        /// Sets the download listener.
        /// </summary>
        public void SetDownloadListener(IAppDownloadListener listener)
        {
            var androidListener = new AppDownloadListener(listener);
            this.ad.Call("setDownloadListener", androidListener);
        }

        /// <summary>
        /// Gets the interaction type.
        /// </summary>
        public int GetInteractionType()
        {
            return this.ad.Call<int>("getInteractionType");
        }

        /// <summary>
        /// Show the reward video Ad.
        /// </summary>
        public void ShowRewardVideoAd()
        {
            var activity = SDK.GetActivity();
            var runnable = new AndroidJavaRunnable(
                () => this.ad.Call("showRewardVideoAd", activity));
            activity.Call("runOnUiThread", runnable);
        }

        /// <summary>
        /// Sets whether to show the download bar.
        /// </summary>
        public void SetShowDownLoadBar(bool show)
        {
            this.ad.Call("setShowDownLoadBar", show);
        }
        /// <summary>
        /// get media extra info dictionary,all value is string type,some need developer cast to real type manually
        /// </summary>
        /// <returns></returns>
        public Dictionary<string, string> GetMediaExtraInfo()
        {
            var map= this.ad.Call<AndroidJavaObject>("getMediaExtraInfo");
            var result = new Dictionary<string, string>();
            var entries = map.Call<AndroidJavaObject>("entrySet").Call<AndroidJavaObject>("iterator");

            while (entries.Call<bool>("hasNext"))
            {
                var entry = entries.Call<AndroidJavaObject>("next");
                var key = entry.Call<AndroidJavaObject>("getKey").Call<string>("toString");
                var value = entry.Call<AndroidJavaObject>("getValue").Call<string>("toString");
                result.Add(key,value);
            }
            return result;
        }
#pragma warning disable SA1300
#pragma warning disable IDE1006

        private sealed class RewardAdInteractionListener : AndroidJavaProxy
        {
            private readonly IRewardAdInteractionListener listener;

            public RewardAdInteractionListener(
                IRewardAdInteractionListener listener)
                : base("com.bytedance.sdk.openadsdk.TTRewardVideoAd$RewardAdInteractionListener")
            {
                this.listener = listener;
            }

            public void onAdShow()
            {
                this.listener.OnAdShow();
            }

            public void onAdVideoBarClick()
            {
                this.listener.OnAdVideoBarClick();
            }

            public void onAdClose()
            {
                this.listener.OnAdClose();
            }

            public void onVideoComplete()
            {
                this.listener.OnVideoComplete();
            }

            public void onVideoError()
            {
                this.listener.OnVideoError();
            }

            public void onRewardVerify(
                bool rewardVerify, int rewardAmount, string rewardName, int errorCode, string errorMsg)
            {
                this.listener.OnRewardVerify(rewardVerify, rewardAmount, rewardName);
            }

            void onSkippedVideo()
            {
                this.listener.OnVideoSkip();
            }
        }

        public void Win(double auctionBidToWin)
        {
            ClientBiddingUtils.Win(ad, auctionBidToWin);
        }

        public void Loss(double auctionPrice = double.NaN, string lossReason = null, string winBidder = null)
        {
            ClientBiddingUtils.Loss(ad, auctionPrice, lossReason, winBidder);
        }

        public void SetPrice(double auctionPrice = double.NaN)
        {
            ClientBiddingUtils.SetPrice(ad, auctionPrice);
        }
#pragma warning restore SA1300
#pragma warning restore IDE1006
    }
#endif
}

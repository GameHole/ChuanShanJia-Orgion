package com.bytedance.android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdDislike;
import com.bytedance.sdk.openadsdk.TTAppDownloadListener;
import com.bytedance.sdk.openadsdk.TTImage;
import com.bytedance.sdk.openadsdk.TTNativeAd;
import com.bytedance.sdk.openadsdk.TTNativeExpressAd;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("EmptyMethod")
public class NativeAdManager {

    private static volatile NativeAdManager sManager;

    private Context mContext;
    private BannerView mBannerView;
    private IntersititialView mIntersititialView;
    private View mExpressView;
    private View mExpressBannerView;
    private Handler mHandler;
    private RequestQueue mQueue;

    private Dialog mAdDialog;

    private NativeAdManager() {
        if (mHandler == null) {
            mHandler = new Handler(Looper.getMainLooper());
        }
    }

    public static NativeAdManager getNativeAdManager() {
        if (sManager == null) {
            synchronized (NativeAdManager.class) {
                if (sManager == null) {
                    sManager = new NativeAdManager();
                }
            }
        }
        return sManager;
    }

    public ViewGroup getRootLayout(Activity context) {
        if (context == null) {
            return null;
        }
        ViewGroup rootGroup = null;
        rootGroup = context.findViewById(android.R.id.content);
        return rootGroup;
    }

    public void addAdView(Activity context, View adView, ViewGroup.LayoutParams layoutParams) {
        if (context == null || adView == null || layoutParams == null) {
            return;
        }
        ViewGroup group = getRootLayout(context);
        if (group == null) {
            return;
        }
        group.addView(adView, layoutParams);
    }

    public void removeAdView(Activity context, View adView) {
        if (context == null || adView == null) {
            return;
        }
        ViewGroup group = getRootLayout(context);
        if (group == null) {
            return;
        }
        group.removeView(adView);
    }

    //????????????????????????????????????????????????????????????destory???????????????
    public void destoryExpressAd(final TTNativeExpressAd nativeExpressAd) {
        if (nativeExpressAd == null) {
            return;
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                nativeExpressAd.destroy();
            }
        });
    }

    //?????????????????????????????????
    public void showNativeBannerAd(final Context context, final TTNativeAd nativeAd) {
        if (context == null || nativeAd == null) {
            return;
        }
        mContext = context;
        if (mQueue == null) {
            mQueue = Volley.newRequestQueue(mContext);
        }

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                removeAdView((Activity) context, mBannerView);
                mBannerView = new BannerView(context);
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(500, 400);
                layoutParams.gravity = Gravity.CENTER | Gravity.BOTTOM;
                addAdView((Activity) context, mBannerView, layoutParams);
                //???????????????????????????
                setBannerAdData(mBannerView, nativeAd);
            }
        });
    }

    //?????????????????????????????????
    public void showNativeIntersititialAd(final Context context, final TTNativeAd nativeAd) {
        if (context == null || nativeAd == null) {
            return;
        }
        mContext = context;
        if (mQueue == null) {
            mQueue = Volley.newRequestQueue(mContext);
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mIntersititialView = new IntersititialView(context);
                mAdDialog = new Dialog(mContext);
                WindowManager.LayoutParams wmParams = mAdDialog.getWindow().getAttributes();
                wmParams.format = PixelFormat.TRANSPARENT;
                wmParams.alpha = 1.0f;//??????????????????1.0??????
                wmParams.width = 900;
                wmParams.height = 600;
                mAdDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                mAdDialog.getWindow().setAttributes(wmParams);
                mAdDialog.setCancelable(false);
                mAdDialog.setContentView(mIntersititialView);
                setIntersititialAdData(mIntersititialView, nativeAd);
                mAdDialog.show();
            }
        });
    }

    //?????????????????????????????????
    public void showExpressFeedAd(final Context context, final TTNativeExpressAd nativeExpressAd,
                                  final TTNativeExpressAd.AdInteractionListener listener,
                                  final TTAdDislike.DislikeInteractionCallback dislikeCallback) {
        if (context == null || nativeExpressAd == null) {
            return;
        }
        mContext = context;
        nativeExpressAd.setExpressInteractionListener(new TTNativeExpressAd.AdInteractionListener() {
             @Override
             public void onAdDismiss() {
             if (listener != null) {
                 listener.onAdDismiss();
                 }
             }

            @Override
            public void onAdClicked(View view, int i) {
                if (listener != null) {
                    listener.onAdClicked(view, i);
                }
            }

            @Override
            public void onAdShow(View view, int i) {
                if (listener != null) {
                    listener.onAdShow(view, i);
                }
            }

            @Override
            public void onRenderFail(View view, String s, int i) {
                if (listener != null) {
                    listener.onRenderFail(view, s, i);
                }
            }

            @Override
            public void onRenderSuccess(final View view, final float v, final float v1) {
                if (listener != null) {
                    listener.onRenderSuccess(view, v, v1);
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        removeAdView((Activity) context, mExpressView);
                        mExpressView = view;
                        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams((int) dip2Px(context, v), (int) dip2Px(context, v1));
                        layoutParams.gravity = Gravity.CENTER | Gravity.BOTTOM;
                        addAdView((Activity) context, mExpressView, layoutParams);
                    }
                });
            }
        });
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                nativeExpressAd.setDislikeCallback((Activity) mContext, new TTAdDislike.DislikeInteractionCallback() {
                    @Override
                    public void onSelected(int i, String s, boolean enforce) {
                        Log.e("unity", "express feed dislike onSelected");
                        if (dislikeCallback != null) {
                            dislikeCallback.onSelected(i, s, enforce);
                        }
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                removeExpressView();
                            }
                        });
                    }

                    @Override
                    public void onCancel() {
                        Log.e("unity", "express feed dislike onCancel");
                        if (dislikeCallback != null) {
                            dislikeCallback.onCancel();
                        }
                    }

                     @Override
                     public void onShow() {
                     Log.e("unity", "express feed dislike onShow");
                     if (dislikeCallback != null) {
                         dislikeCallback.onShow();
                        }
                    }
                });
            }
        });

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                nativeExpressAd.render();
            }
        });

    }
    //?????????????????????????????????
    public void showExpressIntersititalAd(final Context context, final TTNativeExpressAd nativeExpressAd,
                                          final TTNativeExpressAd.AdInteractionListener listener) {
        if (context == null || nativeExpressAd == null) {
            return;
        }
        mContext = context;
        nativeExpressAd.setExpressInteractionListener(new TTNativeExpressAd.AdInteractionListener() {
            @Override
            public void onAdDismiss() {
                Log.e("unity", "Express Intersitital onAdDismiss");
                if (listener != null) {
                    listener.onAdDismiss();
                }
            }

            @Override
            public void onAdClicked(View view, int i) {
                Log.e("unity", "Express Intersitital onAdClicked");
                if (listener != null) {
                    listener.onAdClicked(view, i);
                }
            }

            @Override
            public void onAdShow(View view, int i) {
                 Log.e("unity", "Express Intersitital onAdShow");
                if (listener != null) {
                    listener.onAdShow(view, i);
                }

            }

            @Override
            public void onRenderFail(View view, String s, int i) {
                if (listener != null) {
                    listener.onRenderFail(view, s, i);
                }
                 Log.e("unity", "Express Intersitital onRenderFail");
            }

            @Override
            public void onRenderSuccess(View view, float v, float v1) {
                if (listener != null) {
                    listener.onRenderSuccess(view, v, v1);
                }
                  Log.e("unity", "Express Intersitital onRenderSuccess");
                nativeExpressAd.showInteractionExpressAd((Activity) context);
            }
        });
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                nativeExpressAd.render();
            }
        });
    }

    //?????????????????????????????????
    public void showExpressBannerAd(final Context context, final TTNativeExpressAd nativeExpressAd,
                                    final TTNativeExpressAd.AdInteractionListener listener,
                                    final TTAdDislike.DislikeInteractionCallback dislikeCallback) {
        if (context == null || nativeExpressAd == null) {
            return;
        }
        mContext = context;
        nativeExpressAd.setExpressInteractionListener(new TTNativeExpressAd.AdInteractionListener() {
           @Override
            public void onAdDismiss() {
                Log.e("unity", "Express Banner onAdDismiss");
                if (listener != null) {
                    listener.onAdDismiss();
                }
            }
            @Override
            public void onAdClicked(View view, int i) {
                Log.e("unity", "Express Banner onAdClicked");
                if (listener != null) {
                    listener.onAdClicked(view, i);
                }
            }

            @Override
            public void onAdShow(View view, int i) {
                Log.e("unity", "Express Banner onAdShow");
                if (listener != null) {
                    listener.onAdShow(view, i);
                }
            }

            @Override
            public void onRenderFail(View view, String s, int i) {
                 Log.e("unity", "Express Banner onRenderFail");
                if (listener != null) {
                    listener.onRenderFail(view, s, i);
                }
            }

            @Override
            public void onRenderSuccess(final View view, final float v, final float v1) {
                 Log.e("unity", "Express Banner onRenderSuccess");
                if (listener != null) {
                    listener.onRenderSuccess(view, v, v1);
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        removeAdView((Activity) context, mExpressBannerView);
                        mExpressBannerView = view;
                        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams((int) dip2Px(context, v), (int) dip2Px(context, v1));
                        layoutParams.gravity = Gravity.CENTER | Gravity.BOTTOM;
                        addAdView((Activity) context, mExpressBannerView, layoutParams);
                    }
                });
            }
        });
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                nativeExpressAd.setDislikeCallback((Activity) mContext, new TTAdDislike.DislikeInteractionCallback() {
                    @Override
                    public void onSelected(int i, String s, boolean enforce) {
                        Log.e("unity", "express banner dislike onSelected");
                        if (dislikeCallback != null) {
                            dislikeCallback.onSelected(i, s, enforce);
                        }
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                removeExpressBannerView();
                            }
                        });
                    }

                    @Override
                    public void onCancel() {
                        Log.e("unity", "express banner dislike onCancel");
                        if (dislikeCallback != null) {
                            dislikeCallback.onCancel();
                        }
                    }
                    @Override
                    public void onShow() {
                        Log.e("unity", "express banner dislike onShow");
                        if (dislikeCallback != null) {
                            dislikeCallback.onShow();
                        }
                    }
                });
            }
        });
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                nativeExpressAd.render();
            }
        });
    }

    private void removeBannerView() {
        removeAdView((Activity) mContext, mBannerView);
    }

    private void removeExpressView() {
        removeAdView((Activity) mContext, mExpressView);
    }

    private void removeExpressBannerView() {
        removeAdView((Activity) mContext, mExpressBannerView);
    }

    private float dip2Px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return dipValue * scale + 0.5f;
    }

    private void setBannerAdData(BannerView nativeView, TTNativeAd nativeAd) {
        nativeView.setTitle(nativeAd.getTitle());
        View dislike = nativeView.getDisLikeView();
        Button mCreativeButton = nativeView.getCreateButton();
        bindDislikeAction(nativeAd, dislike, new TTAdDislike.DislikeInteractionCallback() {
            @Override
            public void onSelected(int position, String value, boolean enforce) {
                Log.e("unity", "banner dislike onSelected");
                removeBannerView();
            }

            @Override
            public void onCancel() {
                Log.e("unity", "banner dislike onCancel");
            }

            @Override
            public void onShow() {
                Log.e("unity", "banner dislike onShow");
            }
        });
        if (nativeAd.getImageList() != null && !nativeAd.getImageList().isEmpty()) {
            TTImage image = nativeAd.getImageList().get(0);
            if (image != null && image.isValid()) {
                ImageView im = nativeView.getImageView();
                loadImgByVolley(image.getImageUrl(), im, 300, 200);
            }
        }
        //???????????????????????????????????????????????????????????????
        switch (nativeAd.getInteractionType()) {
            case TTAdConstant.INTERACTION_TYPE_DOWNLOAD:
                //???????????????ttAdManager.createAdNative(getApplicationContext())????????????activity ??????????????????activity?????????????????????Dislike??????
                nativeAd.setActivityForDownloadApp((Activity) mContext);
                mCreativeButton.setVisibility(View.VISIBLE);
                nativeAd.setDownloadListener(new MyDownloadListener(mCreativeButton)); // ?????????????????????
                break;
            case TTAdConstant.INTERACTION_TYPE_DIAL:
                mCreativeButton.setVisibility(View.VISIBLE);
                mCreativeButton.setText("????????????");
                break;
            case TTAdConstant.INTERACTION_TYPE_LANDING_PAGE:
            case TTAdConstant.INTERACTION_TYPE_BROWSER:
                mCreativeButton.setVisibility(View.VISIBLE);
                mCreativeButton.setText("????????????");
                break;
            default:
                mCreativeButton.setVisibility(View.GONE);
        }

        //??????????????????view, ????????????nativeView?????????????????????????????????????????????
        List<View> clickViewList = new ArrayList<>();
        clickViewList.add(nativeView);

        //?????????????????????view?????????????????????????????????
        List<View> creativeViewList = new ArrayList<>();
        //????????????????????????????????????????????????????????????????????????????????????????????????view??????
        //creativeViewList.add(nativeView);
        creativeViewList.add(mCreativeButton);

        //??????! ???????????????????????????????????????????????????convertView????????????ViewGroup???
        nativeAd.registerViewForInteraction((ViewGroup) nativeView, clickViewList, creativeViewList, dislike, new TTNativeAd.AdInteractionListener() {
            @Override
            public void onAdClicked(View view, TTNativeAd ad) {
                if (ad != null) {
                    Toast.makeText(mContext, "??????" + ad.getTitle() + "?????????", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAdCreativeClick(View view, TTNativeAd ad) {
                if (ad != null) {
                    Toast.makeText(mContext, "??????" + ad.getTitle() + "????????????????????????", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAdShow(TTNativeAd ad) {
                if (ad != null) {
                    Toast.makeText(mContext, "??????" + ad.getTitle() + "??????", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void setIntersititialAdData(IntersititialView nativeView, TTNativeAd nativeAd) {
        nativeView.setTitle(nativeAd.getTitle());
        View dislike = nativeView.getDisLikeView();
        View close = nativeView.getClose();
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAdDialog != null) {
                    mAdDialog.dismiss();
                }
            }
        });
        Button mCreativeButton = nativeView.getCreateButton();
        bindDislikeAction(nativeAd, dislike, new TTAdDislike.DislikeInteractionCallback() {
            @Override
            public void onSelected(int position, String value, boolean enforce) {
                Log.e("unity", "Intersititial dislike onSelected");
                if (mAdDialog != null) {
                    mAdDialog.dismiss();
                }
            }

            @Override
            public void onCancel() {
                 Log.e("unity", "Intersititial dislike onCancel");
            }

            @Override
            public void onShow() {
                Log.e("unity", "Intersititial dislike onShow");
            }
        });
        if (nativeAd.getImageList() != null && !nativeAd.getImageList().isEmpty()) {
            TTImage image = nativeAd.getImageList().get(0);
            if (image != null && image.isValid()) {
                ImageView im = nativeView.getImageView();
                loadImgByVolley(image.getImageUrl(), im, 900, 600);
            }
        }
        //???????????????????????????????????????????????????????????????
        switch (nativeAd.getInteractionType()) {
            case TTAdConstant.INTERACTION_TYPE_DOWNLOAD:
                //???????????????ttAdManager.createAdNative(getApplicationContext())????????????activity ??????????????????activity?????????????????????Dislike??????
                nativeAd.setActivityForDownloadApp((Activity) mContext);
                mCreativeButton.setVisibility(View.VISIBLE);
                nativeAd.setDownloadListener(new MyDownloadListener(mCreativeButton)); // ?????????????????????
                break;
            case TTAdConstant.INTERACTION_TYPE_DIAL:
                mCreativeButton.setVisibility(View.VISIBLE);
                mCreativeButton.setText("????????????");
                break;
            case TTAdConstant.INTERACTION_TYPE_LANDING_PAGE:
            case TTAdConstant.INTERACTION_TYPE_BROWSER:
                mCreativeButton.setVisibility(View.VISIBLE);
                mCreativeButton.setText("????????????");
                break;
            default:
                mCreativeButton.setVisibility(View.GONE);
        }

        //??????????????????view, ????????????nativeView?????????????????????????????????????????????
        List<View> clickViewList = new ArrayList<>();
        clickViewList.add(nativeView);

        //?????????????????????view?????????????????????????????????
        List<View> creativeViewList = new ArrayList<>();
        //????????????????????????????????????????????????????????????????????????????????????????????????view??????
        //creativeViewList.add(nativeView);
        creativeViewList.add(mCreativeButton);

        //??????! ???????????????????????????????????????????????????convertView????????????ViewGroup???
        nativeAd.registerViewForInteraction((ViewGroup) nativeView, clickViewList, creativeViewList, dislike, new TTNativeAd.AdInteractionListener() {
            @Override
            public void onAdClicked(View view, TTNativeAd ad) {
                Log.e("unity", "Intersititial onAdClicked");
                if (ad != null) {
                    Toast.makeText(mContext, "??????" + ad.getTitle() + "?????????", Toast.LENGTH_SHORT).show();
                }
                if (mAdDialog != null) {
                    mAdDialog.dismiss();
                }
            }

            @Override
            public void onAdCreativeClick(View view, TTNativeAd ad) {
                  Log.e("unity", "Intersititial onAdCreativeClick");
                if (ad != null) {
                    Toast.makeText(mContext, "??????" + ad.getTitle() + "????????????????????????", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAdShow(TTNativeAd ad) {
                 Log.e("unity", "Intersititial onAdShow");
                if (ad != null) {
                    Toast.makeText(mContext, "??????" + ad.getTitle() + "??????", Toast.LENGTH_SHORT).show();
                }
                //???????????????flags?????????????????????
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        WindowManager.LayoutParams wmParams = mAdDialog.getWindow().getAttributes();
                        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                        mAdDialog.getWindow().setAttributes(wmParams);
                    }
                });
            }
        });

    }

    //???????????????dislike ?????????????????????????????????????????????
    private void bindDislikeAction(TTNativeAd ad, View dislikeView, TTAdDislike.DislikeInteractionCallback callback) {
        final TTAdDislike ttAdDislike = ad.getDislikeDialog((Activity) mContext);
        if (ttAdDislike != null) {
            ttAdDislike.setDislikeInteractionCallback(callback);
        }
        dislikeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                  Log.e("unity", "dislikeView onClick");
                if (ttAdDislike != null)
                    ttAdDislike.showDislikeDialog();
            }
        });
    }

    static class MyDownloadListener implements TTAppDownloadListener {
        Button mDownloadButton;
        Handler mHandler;

        public MyDownloadListener(Button button) {
            mDownloadButton = button;
            mHandler = new Handler(Looper.getMainLooper());
        }

        @Override
        public void onIdle() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mDownloadButton != null) {
                        mDownloadButton.setText("????????????");
                    }
                }
            });
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onDownloadActive(final long totalBytes, final long currBytes, String fileName, String appName) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mDownloadButton != null) {
                        if (totalBytes <= 0L) {
                            mDownloadButton.setText("????????? percent: 0");
                        } else {
                            if (totalBytes > 0) {
                                 mDownloadButton.setText("????????? percent: " + (currBytes * 100 / totalBytes));
                            }
                        }
                    }
                }
            });
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onDownloadPaused(final long totalBytes, final long currBytes, String fileName, String appName) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mDownloadButton != null) {
                        if (totalBytes > 0) {
                              mDownloadButton.setText("???????????? percent: " + (currBytes * 100 / totalBytes));
                        }
                    }
                }
            });
        }

        @Override
        public void onDownloadFailed(long totalBytes, long currBytes, String fileName, String appName) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mDownloadButton != null) {
                        mDownloadButton.setText("????????????");
                    }
                }
            });
        }

        @Override
        public void onInstalled(String fileName, String appName) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mDownloadButton != null) {
                        mDownloadButton.setText("????????????");
                    }
                }
            });
        }

        @Override
        public void onDownloadFinished(long totalBytes, String fileName, String appName) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mDownloadButton != null) {
                        mDownloadButton.setText("????????????");
                    }
                }
            });
        }
    }

    public void loadImgByVolley(String imgUrl, final ImageView imageView, int maxWidth, int maxHeight) {
        ImageRequest imgRequest = new ImageRequest(imgUrl,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(final Bitmap arg0) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                imageView.setImageBitmap(arg0);
                            }
                        });
                    }
                }, maxWidth, maxHeight, Bitmap.Config.ARGB_8888,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError arg0) {
                    }
                });
        mQueue.add(imgRequest);
    }
}

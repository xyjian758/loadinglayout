package com.qing.xyjian.loadinglayout;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;



/**
 * 2018/5/30  17:37
 * Xyjian
 * 类说明：{页面初始化加载布局显示：加载中...  无网络 无数据 正常显示当前页面}
 */
@SuppressWarnings("ALL")
public class LoadingLayout extends FrameLayout {

    public final static int Success = 0;//显示数据内容
    public final static int Anim = 1;//现在加载动画
    public final static int NoNet = 2;//没有网络
    public final static int NoData = 3;//没有数据


    private int animationTime = 500;//默认值
    private int state = -1;//默认是-1

    private Context mContext;
    private View noNetPage;
    private View animPage;
    private View contentView;
    private View noDataPage;

    private boolean loading_anim_firstvisble; //是否默认显示 加载布局 默认显示加载中的布局
    private boolean loading_no_net_need;//是否需要 无网络页面布局 默认是
    private boolean loading_anim_need;//是否需要 动画加载中布局 默认是
    private boolean loading_no_data_need;//是否需要 显示空数据样式  根据资源ID判断
    private int loading_anim_resourceId;
    private int loading_no_net_resourceId;
    private int loading_no_data_resourceId;
    private OnReLoadListener mOnReloadListener;//默认无网络布局重新加载按钮响应事件


    public LoadingLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LoadingLayout);
        loading_anim_firstvisble = a.getBoolean(R.styleable.LoadingLayout_loading_anim_firstvisble, true);
        loading_anim_need = a.getBoolean(R.styleable.LoadingLayout_loading_anim_need, true);
        loading_no_net_need = a.getBoolean(R.styleable.LoadingLayout_loading_no_net_need, true);
        loading_anim_resourceId = a.getResourceId(R.styleable.LoadingLayout_loading_anim, 0);
        loading_no_net_resourceId = a.getResourceId(R.styleable.LoadingLayout_loading_no_net, 0);
        loading_no_data_resourceId = a.getResourceId(R.styleable.LoadingLayout_loading_no_data, 0);
        a.recycle();
    }

    public LoadingLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
    }

    public LoadingLayout(Context context) {
        super(context);
        this.mContext = context;
    }

    @Override
    public void onViewAdded(View child) {
        super.onViewAdded(child);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildCount() > 1) {
            throw new IllegalStateException("LoadingLayout can host only one direct child");
        }
        contentView = this.getChildAt(0);
        build();
        if (loading_anim_firstvisble) {
            setStatus(Anim);
        } else {
            setStatus(Success);
        }
    }

    private void build() {
        //动画布局加载
        if (loading_anim_need) {
            if (loading_anim_resourceId != 0) {
                //加载指向的动画布局
                animPage = LayoutInflater.from(mContext).inflate(loading_anim_resourceId, null);
            } else {
                //加载默认的动画布局
                animPage = LayoutInflater.from(mContext).inflate(R.layout.include_loading_anim, null);
            }
            this.addView(animPage);
        }
        //无网络布局加载
        if (loading_no_net_need) {
            if (loading_no_net_resourceId != 0) {
                //加载指向的无网络布局
                noNetPage = LayoutInflater.from(mContext).inflate(loading_no_net_resourceId, null);
            } else {
                //加载默认的无网络布局
                noNetPage = LayoutInflater.from(mContext).inflate(R.layout.include_no_network, null);
                noNetPage.findViewById(R.id.tv_no_network).setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mOnReloadListener != null)
                            mOnReloadListener.onReload();
                    }
                });
            }
            this.addView(noNetPage);
        }

        //无数据显示页面布局加载
        if (loading_no_data_resourceId != 0) {
            loading_no_data_need = true;
            noDataPage = LayoutInflater.from(mContext).inflate(loading_no_data_resourceId, null);
            this.addView(noDataPage);
        } else {
            loading_no_data_need = false;
        }

    }

    public void setStatus(@Flavour int status) {

        if (!loading_anim_need && !loading_no_net_need && !loading_no_data_need) {
            //没有设置任何加载后的样式显示就不需要走了
            return;
        }
        switch (status) {
            case Success:
                if (this.state == Success) {
                    break;
                }
                setAlpha(contentView);
                if (loading_anim_need) {
                    setViewGone(animPage);
                }

                if (loading_no_net_need) {
                    setViewGone(noNetPage);
                }
                if (loading_no_data_need) {
                    setViewGone(noDataPage);
                }
                break;

            case Anim:
                if (!loading_anim_need) {
                    break;
                }
                if (this.state == Anim) {
                    break;
                }
                setAlpha(animPage);
                setViewGone(contentView);
                if (loading_no_net_need) {
                    setViewGone(noNetPage);
                }
                if (loading_no_data_need) {
                    setViewGone(noDataPage);
                }
                break;

            case NoNet:
                if (!loading_no_net_need) {
                    //不需要显示错误布局 就直接不处理了
                    break;
                }
                if (this.state == NoNet) {
                    break;
                }
                setAlpha(noNetPage);
                if (loading_anim_need) {
                    setViewGone(animPage);
                }
                setViewGone(contentView);

                if (loading_no_data_need) {
                    setViewGone(noDataPage);
                }

                break;
            case NoData:
                if (!loading_no_data_need) {
                    //不需要显示空数据布局 就直接不处理了
                    break;
                }
                if (this.state == NoData) {
                    break;
                }
                setAlpha(noDataPage);

                if (loading_anim_need) {
                    setViewGone(animPage);
                }
                if (loading_no_net_need) {
                    setViewGone(noNetPage);
                }

                setViewGone(contentView);

                break;

            default:
                break;
        }
        this.state = status;
    }

    private void setViewVisible(View view) {
        if (null != view) {
            if (view.getVisibility() != View.VISIBLE) {
                view.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setViewGone(View view) {
        if (null != view) {
            if (view.getVisibility() != View.GONE) {
                view.setVisibility(View.GONE);
            }
        }
    }

    @IntDef({Success, Anim, NoNet, NoData})
    public @interface Flavour {

    }

    /**
     * 2018/5/22  18:22
     * 方法说明：{设置默认无网络页面时重新加载按钮的响应事件，若是手动指定无网络的布局，则无法响应}
     */

    public void setOnReLoadListener(OnReLoadListener onReLoadListener) {
        this.mOnReloadListener = onReLoadListener;
    }


    //设置动画渐变时间
    public void setAnimationTime(int animationTime) {
        this.animationTime = animationTime;
    }

    private void setAlpha(final View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", 0, 1);
        animator.setDuration(animationTime);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                setViewVisible(view);
            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.start();
    }

    public interface OnReLoadListener {
        void onReload();
    }

}

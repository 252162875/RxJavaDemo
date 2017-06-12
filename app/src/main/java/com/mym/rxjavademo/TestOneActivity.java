package com.mym.rxjavademo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.mym.rxjavademo.bean.DouBanBean;
import com.mym.rxjavademo.http.Api;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class TestOneActivity extends AppCompatActivity {
    StringBuilder stringBuilder;
    CompositeDisposable compositeDisposable;
    @BindView(R.id.tv_info)
    TextView tvInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        compositeDisposable = new CompositeDisposable();
        setContentView(R.layout.activity_test_one);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.btn_start, R.id.btn_http})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_start:
                stringBuilder = new StringBuilder();
                doTest1();
                break;
            case R.id.btn_http:

                getDouban();
                break;
        }
    }

    /***
     * 请求网络获得豆瓣TOP250
     */
    private void getDouban() {
        stringBuilder = new StringBuilder();
        final Api api = Api.Builder.getService();

        api.getTopMovie(0, 30)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnNext(new Consumer<DouBanBean>() {
                    @Override
                    public void accept(@NonNull DouBanBean douBanBean) throws Exception {
                        Toast.makeText(TestOneActivity.this, "这里是模拟注册，数据成功返回了", Toast.LENGTH_SHORT).show();//先根据注册的响应结果去做一些操作
                        stringBuilder.append(douBanBean.getTitle() + "模拟注册\r\ncount:" + douBanBean.getCount() + "\r\n");
                        for (int i = 0; i < douBanBean.getSubjects().size(); i++) {
                            stringBuilder.append(i + 1 + "、" + douBanBean.getSubjects().get(i).getTitle() + "\r\n");
                            tvInfo.setText(stringBuilder);
                        }
                    }
                })
                .observeOn(Schedulers.io())                 //回到IO线程去发起登录请求
                .flatMap(new Function<DouBanBean, ObservableSource<DouBanBean>>() {
                    @Override
                    public ObservableSource<DouBanBean> apply(@NonNull DouBanBean douBanBean) throws Exception {
                        return api.getTopMovie(31, 60);
                    }
                }).observeOn(AndroidSchedulers.mainThread())//回到主线程去处理请求登录的结果
                .subscribe(new Observer<DouBanBean>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        //最先执行的，切断以后下面的不会调用
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(@NonNull DouBanBean douBanBean) {

                        stringBuilder.append(douBanBean.getTitle() + "模拟登陆\r\ncount:" + douBanBean.getCount() + "\r\n");
                        for (int i = 0; i < douBanBean.getSubjects().size(); i++) {
                            stringBuilder.append(i + 1 + "、" + douBanBean.getSubjects().get(i).getTitle() + "\r\n");
                            tvInfo.setText(stringBuilder);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    /***
     * 最基本的订阅绑定
     */
    private void doTest1() {
        //创建一个上游 Observable：
        Observable<String> stringObservable = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<String> e) throws Exception {
                stringBuilder.append("subscribe" + "---" + Thread.currentThread().getName() + "\r\n");
                e.onNext("1");
                e.onNext("2");
                e.onNext("3");
                e.onNext("4");
                e.onComplete();
            }
        }).concatMap(new Function<String, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(@NonNull String s) throws Exception {

                final List<String> list = new ArrayList<>();
                for (int i = 0; i < 3; i++) {
                    list.add("I am value ---" + s);
                }
                return Observable.fromIterable(list).delay(10, TimeUnit.MILLISECONDS);//concatMap是有序的

            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
        //创建一个下游 Observer
        Observer<String> stringObserver = new Observer<String>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
            }

            @Override
            public void onNext(@NonNull String s) {
                stringBuilder.append(s + "\r\n");
            }

            @Override
            public void onError(@NonNull Throwable e) {
                stringBuilder.append(e.toString() + "\r\n");
            }

            @Override
            public void onComplete() {
                stringBuilder.append("onComplete" + "---" + Thread.currentThread().getName() + "\r\n");
                tvInfo.setText(stringBuilder);
                doTest2();
            }
        };
        stringObservable.subscribe(stringObserver);
    }

    /***
     * 链式调用
     */
    private void doTest2() {
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<String> e) throws Exception {
                stringBuilder.append("subscribe" + "---" + Thread.currentThread().getName() + "\r\n");
                e.onNext("a");
                e.onNext("b");
                e.onNext("c");
                e.onNext("d");
                e.onComplete();
            }
        }).flatMap(new Function<String, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(@NonNull String s) throws Exception {
                final List<String> list = new ArrayList<>();
                for (int i = 0; i < 3; i++) {
                    list.add("I am value ---" + s);
                }
                return Observable.fromIterable(list).delay(10, TimeUnit.MILLISECONDS);//为了展示flatMap的无序性，在这加了10毫秒延迟
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io()).subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
            }

            @Override
            public void onNext(@NonNull String s) {
                stringBuilder.append(s + "\r\n");
            }

            @Override
            public void onError(@NonNull Throwable e) {
                stringBuilder.append(e.toString() + "\r\n");
            }

            @Override
            public void onComplete() {
                stringBuilder.append("onComplete" + "---" + Thread.currentThread().getName() + "\r\n");
                tvInfo.setText(stringBuilder);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();//这里调用后当activity关闭，则接下来的onNext等方法不会执行（相当于切断了水管），但是上面没发完的请求还是继续发，只是下面的接收不到响应（大概就这么个意思吧）
    }
}

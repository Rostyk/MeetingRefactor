package com.exchange.ross.exchangeapp.core.model;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.exchange.ross.exchangeapp.activity.Views.EventsFragment;

/**
 * Created by ross on 4/4/15.
 */
public class MyPageAdapter extends FragmentStatePagerAdapter implements ViewPager.OnPageChangeListener{

    SparseArray<android.support.v4.app.Fragment> registeredFragments = new SparseArray<android.support.v4.app.Fragment>();
    private Boolean firstPageLoad = true;
    private Activity activity;
    private int positionCurrent;
    private boolean dontLoadList = false;
    public MyPageAdapter(android.support.v4.app.FragmentManager fm) {
        super(fm);
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return 30;
    }


    @Override
    public android.support.v4.app.Fragment getItem(int position) {
        EventsFragment fragment = EventsFragment.newInstance(null, null, positionCurrent, this.activity);
        fragment.setActivity(activity);
        fragment.setPosition(position);
        return fragment;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        EventsFragment fragment = (EventsFragment) super.instantiateItem(container, position);
        fragment.setActivity(activity);
        fragment.setPosition(position);
        registeredFragments.put(position, fragment);
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        registeredFragments.remove(position);

        if (position <= getCount()) {
            FragmentManager manager = ((Fragment) object).getFragmentManager();
            android.support.v4.app.FragmentTransaction trans = manager.beginTransaction();
            trans.remove((Fragment) object);
            trans.commit();
        }

        super.destroyItem(container, position, object);
    }

    public android.support.v4.app.Fragment getRegisteredFragment(int position) {
        return registeredFragments.get(position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "OBJECT " + (position + 1);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (state == 0) { // the viewpager is idle as swipping ended
           new Handler().postDelayed(new Runnable() {
                public void run() {
                    if (!dontLoadList) {
                        loadList();
                    }
                }
            }, 200);
        }
    }

    public void loadList() {

        new Handler().postDelayed(new Runnable() {
            public void run() {
                EventsFragment cachedFragment = (EventsFragment)getRegisteredFragment(positionCurrent);
                if(cachedFragment == null)
                    cachedFragment = (EventsFragment)getItem(positionCurrent);

                cachedFragment.setPosition(positionCurrent);
                cachedFragment.setActivity(activity);
                cachedFragment.setupEvents();
            }
        },40);


    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        positionCurrent = position;
        if( positionOffset == 0 && positionOffsetPixels == 0 ) {
            dontLoadList = false;

            if (position == 1 && firstPageLoad) {
                loadList();
                firstPageLoad = false;
            }

        }
        else {
            dontLoadList = true; // To avoid loading content for list after swiping the pager.
        }
    }

    @Override
    public void onPageSelected(int position) {

    }





}

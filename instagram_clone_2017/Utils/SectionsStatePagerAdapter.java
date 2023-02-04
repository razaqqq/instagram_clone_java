package com.example.instagram_clone_2017.Utils;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SectionsStatePagerAdapter extends FragmentStatePagerAdapter {

    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final HashMap<Fragment, Integer> mFragments = new HashMap<>();
    private final HashMap<String, Integer> mFragmentsNumber = new HashMap<>();
    private final HashMap<Integer, String> mFragmentsNames = new HashMap<>();

    public SectionsStatePagerAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    public void addFragment(Fragment fragment, String fragmentNames)
    {
        mFragmentList.add(fragment);
        mFragments.put(fragment, mFragmentList.size()-1);
        mFragmentsNumber.put(fragmentNames, mFragmentList.size()-1);
        mFragmentsNames.put(mFragmentList.size()-1, fragmentNames);

    }

    public Integer getFragmentNumber(String FragmentNames)
    {
        if (mFragmentsNumber.containsKey(mFragmentsNames))
        {
            return mFragmentsNumber.get(mFragmentsNames);
        }
        else
        {
            return null;
        }
    }

    public Integer getFragmentNumber(Fragment Fragment)
    {
        if (mFragmentsNumber.containsKey(mFragments))
        {
            return mFragmentsNumber.get(mFragments);
        }
        else
        {
            return null;
        }
    }

    public String getFragmentName(Integer fragmentNumber)
    {
        if (mFragmentsNames.containsKey(mFragmentsNumber))
        {
            return mFragmentsNames.get(mFragmentsNumber);
        }
        else
        {
            return null;
        }
    }

}

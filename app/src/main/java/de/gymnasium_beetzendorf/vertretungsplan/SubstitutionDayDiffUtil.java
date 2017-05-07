package de.gymnasium_beetzendorf.vertretungsplan;

import android.support.v7.util.DiffUtil;

import java.util.List;

import de.gymnasium_beetzendorf.vertretungsplan.data.SubstitutionDay;

/**
 * Created by davidfrenzel on 26.04.17.
 */

public class SubstitutionDayDiffUtil extends DiffUtil.Callback {

    List<SubstitutionDay> oldSubstitutionDayList;
    List<SubstitutionDay> newSubstitutionDayList;


    public SubstitutionDayDiffUtil(List<SubstitutionDay> oldSubstitutionDayList, List<SubstitutionDay> newSubstitutionDayList) {
        this.oldSubstitutionDayList = oldSubstitutionDayList;
        this.newSubstitutionDayList = newSubstitutionDayList;
    }

    @Override
    public int getOldListSize() {
        return oldSubstitutionDayList.size();
    }

    @Override
    public int getNewListSize() {
        return newSubstitutionDayList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return false;
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return false;
    }
}

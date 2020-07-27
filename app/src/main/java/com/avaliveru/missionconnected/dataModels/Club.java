package com.avaliveru.missionconnected.dataModels;

public class Club implements Comparable<Club> {

    public String clubName;
    public String clubPreview;
    public String clubDescription;
    public String clubImageURL;
    public String clubID;
    public long numberOfMembers;

    @Override
    public int compareTo(Club club) {
        return this.clubName.compareTo(club.clubName);
    }
}

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;

public class ClubInfo {
    private static int clubId;
    private static String password;
    private static int maxConnection;
    private static int connectedNum;

    private static volatile ChannelGroup channelGroup;

    ClubInfo(final int id, String pwd, int max, ChannelGroup cg) {
        clubId = id;
        password = pwd;
        maxConnection = max;
        channelGroup = cg;
    }

    int getClubId() {
        return clubId;
    }

    boolean isValid(String pwd) {
        return pwd.equals(password);
    }

    boolean reachedMax() {
        return connectedNum >= maxConnection;
    }

    void addChannelGroup(Channel channel) {
        channelGroup.add(channel);
        connectedNum++;
    }

    void writeGroupChannel(String msg) {
        channelGroup.writeAndFlush(msg);
    }

}

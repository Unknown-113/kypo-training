package cz.muni.ics.kypo.training.utils;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * This class is a representation of a logged command retrieved by the elastic search api.
 */
public class ElasticSearchCommand {
    private String hostname;
    private String ip;
    @JsonProperty(value = "timestamp_str")
    private String timestampStr;
    @JsonProperty(value = "sandbox_id")
    private String sandboxId;
    private String cmd;
    @JsonProperty(value = "pool_id")
    private Long poolId;
    private String wd;
    @JsonProperty(value = "cmd_type")
    private String cmdType;
    private String username;

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getTimestampStr() {
        return timestampStr;
    }

    public void setTimestampStr(String timestampStr) {
        this.timestampStr = timestampStr;
    }

    public String getSandboxId() {
        return sandboxId;
    }

    public void setSandboxId(String sandboxId) {
        this.sandboxId = sandboxId;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public Long getPoolId() {
        return poolId;
    }

    public void setPoolId(Long poolId) {
        this.poolId = poolId;
    }

    public String getWd() {
        return wd;
    }

    public void setWd(String wd) {
        this.wd = wd;
    }

    public String getCmdType() {
        return cmdType;
    }

    public void setCmdType(String cmdType) {
        this.cmdType = cmdType;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ElasticSearchCommand that = (ElasticSearchCommand) o;
        return hostname.equals(that.hostname) && ip.equals(that.ip) && timestampStr.equals(that.timestampStr) && sandboxId.equals(that.sandboxId) && cmd.equals(that.cmd) && poolId.equals(that.poolId) && wd.equals(that.wd) && cmdType.equals(that.cmdType) && username.equals(that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hostname, ip, timestampStr, sandboxId, cmd, poolId, wd, cmdType, username);
    }

    @Override
    public String toString() {
        return "ElasticSearchCommand{" +
                "hostname='" + hostname + '\'' +
                ", ip='" + ip + '\'' +
                ", timestampStr='" + timestampStr + '\'' +
                ", sandboxId=" + sandboxId +
                ", cmd='" + cmd + '\'' +
                ", poolId=" + poolId +
                ", wd='" + wd + '\'' +
                ", cmdType='" + cmdType + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}

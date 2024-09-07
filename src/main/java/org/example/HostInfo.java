package org.example;

/**
 * Клас для зберігання інформації про хост, його адресу, MAC-адресу та віддалену адресу.
 */
public class HostInfo {

    // Поле для зберігання імені хоста
    private String hostName;

    // Поле для зберігання IP-адреси хоста
    private String hostAddress;

    // Поле для зберігання MAC-адреси хоста
    private String macAddress;

    // Поле для зберігання віддаленої IP-адреси
    private String remoteAddr;

    /**
     * Конструктор для ініціалізації всіх полів класу.
     *
     * @param hostName Ім'я хоста
     * @param hostAddress IP-адреса хоста
     * @param macAddress MAC-адреса хоста
     * @param remoteAddr Віддалена IP-адреса
     */
    public HostInfo(String hostName, String hostAddress, String macAddress, String remoteAddr) {
        this.hostName = hostName;
        this.hostAddress = hostAddress;
        this.macAddress = macAddress;
        this.remoteAddr = remoteAddr;
    }

    /**
     * Повертає ім'я хоста.
     *
     * @return Ім'я хоста
     */
    public String getHostName() {
        return hostName;
    }

    /**
     * Встановлює нове ім'я хоста.
     *
     * @param hostName Нове ім'я хоста
     */
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    /**
     * Повертає IP-адресу хоста.
     *
     * @return IP-адреса хоста
     */
    public String getHostAddress() {
        return hostAddress;
    }

    /**
     * Встановлює нову IP-адресу хоста.
     *
     * @param hostAddress Нова IP-адреса хоста
     */
    public void setHostAddress(String hostAddress) {
        this.hostAddress = hostAddress;
    }

    /**
     * Повертає MAC-адресу хоста.
     *
     * @return MAC-адреса хоста
     */
    public String getMacAddress() {
        return macAddress;
    }

    /**
     * Встановлює нову MAC-адресу хоста.
     *
     * @param macAddress Нова MAC-адреса хоста
     */
    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    /**
     * Повертає віддалену IP-адресу.
     *
     * @return Віддалена IP-адреса
     */
    public String getRemoteAddr() {
        return remoteAddr;
    }

    /**
     * Встановлює нову віддалену IP-адресу.
     *
     * @param remoteAddr Нова віддалена IP-адреса
     */
    public void setRemoteAddr(String remoteAddr) {
        this.remoteAddr = remoteAddr;
    }

    @Override
    public String toString() {
        return "HostInfo{" +
                "hostName='" + hostName + '\'' +
                ", hostAddress='" + hostAddress + '\'' +
                ", macAddress='" + macAddress + '\'' +
                ", remoteAddr='" + remoteAddr + '\'' +
                '}';
    }
}

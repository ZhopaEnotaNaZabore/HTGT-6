package com.mod.htgt6.common.block.greg.technological.hulls;

/** Tier-sized EU buffer; shared 1 A tick limits across IC2 and GT6 access. */
public final class HullEnergyBuffer {
    private double energy, voltage, freshEnergy;
    private long capacity;
    public HullEnergyBuffer(long capacity) { setCapacity(capacity); }
    public void setCapacity(long capacity) {
        this.capacity=Math.max(0,capacity);
        energy=Math.min(energy,this.capacity);
        if (energy == 0) voltage=0;
    }
    private long received = Long.MIN_VALUE, sent = Long.MIN_VALUE;
    public double energy() { return energy; }
    public double voltage() { return voltage; }
    public boolean canReceive(long tick) { return received != tick && energy < capacity; }
    public double offer(long tick) {
        return sent != tick ? Math.min(voltage, Math.max(0, energy - (received == tick ? freshEnergy : 0))) : 0;
    }
    public double receive(long tick, long maximum, double amount, double packetVoltage, boolean commit) {
        if (!canReceive(tick) || !Double.isFinite(amount) || !Double.isFinite(packetVoltage)
                || amount <= 0 || packetVoltage <= 0 || packetVoltage > maximum
                || energy > 0 && packetVoltage != voltage) return 0;
        double accepted = Math.min(Math.min(amount, packetVoltage), capacity - energy);
        if (commit) { energy += accepted; voltage = packetVoltage; received = tick; freshEnergy = accepted; }
        return accepted;
    }
    public void consume(long tick, double amount) {
        if (!Double.isFinite(amount) || amount <= 0) return;
        double offered = offer(tick);
        if (offered <= 0) return;
        energy = Math.max(0, energy - Math.min(offered, amount));
        sent = tick;
        if (energy == 0) voltage = 0;
    }
    public void restore(long maximum, double savedEnergy, double savedVoltage) {
        energy = voltage = freshEnergy = 0;
        received = sent = Long.MIN_VALUE;
        if (Double.isFinite(savedVoltage) && savedVoltage > 0 && savedVoltage <= maximum
                && Double.isFinite(savedEnergy) && savedEnergy > 0) {
            voltage = savedVoltage;
            energy = Math.min(savedEnergy, capacity);
        }
    }
}

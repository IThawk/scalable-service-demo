package com.scalable.c1发号器.provider;

/**基于属性配置进行实现的，也是一种用于测试环境的方式，使用这种方式，需要在部署的每台机器上配置不同的机器号，在生产环境中是不实现的。
 * @author Administrator
 *
 */
public class PropertyMachineIdProvider implements MachineIdProvider {
	
    private long machineId;

    public long getMachineId() {
        return machineId;
    }

    public void setMachineId(long machineId) {
        this.machineId = machineId;
    }
    
}
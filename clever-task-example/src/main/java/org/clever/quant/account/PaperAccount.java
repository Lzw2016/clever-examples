package org.clever.quant.account;

/**
 * 模拟账号
 * <p>
 * 作者：lizw <br/>
 * 创建时间：2026/02/27 20:16 <br/>
 */
public class PaperAccount extends AbstractAccount {
    /**
     * @param totalAmount 初始总金额
     */
    public PaperAccount(double totalAmount) {
        this(totalAmount, null);
    }

    /**
     * @param totalAmount 初始总金额
     * @param name        账户名
     */
    public PaperAccount(double totalAmount, String name) {
        super(totalAmount, name);
    }
}

package org.rill.bpm.api.processvar;

import java.io.Serializable;

public class DummyOrder implements Serializable {

	public static final int BU_TI = 0;
	public static final int XIAN_TI = 1;
	public static final int HOU_TI = 2;
	
	private int isNeedGift;

	public final int getIsNeedGift() {
		return isNeedGift;
	}

	public final void setIsNeedGift(int isNeedGift) {
		this.isNeedGift = isNeedGift;
	}
	
	
}

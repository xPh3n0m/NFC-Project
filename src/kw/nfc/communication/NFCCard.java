package kw.nfc.communication;

import javax.smartcardio.ATR;
import javax.smartcardio.Card;

import org.json.simple.JSONObject;

public class NFCCard {
	
	private Card card;
	private ATR atr;
	private String data;
	
	public NFCCard(Card card, ATR atr, String data) {
		this.card = card;
		this.atr = atr;
		this.data = data;
	}

	public String getData() {
		return data;
	}
	
	public boolean equals(NFCCard otherCard) {
		if(otherCard == null) {
			return false;
		}
		
		if(atr.equals(otherCard.getATR())) {
			return true;
		} else {
			return false;
		}
	}

	public ATR getATR() {
		return atr;
	}

	public Card getCard() {
		// TODO Auto-generated method stub
		return card;
	}

}

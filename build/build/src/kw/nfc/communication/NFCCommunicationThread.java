package kw.nfc.communication;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import javax.smartcardio.ATR;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.smartcardio.TerminalFactory;

import org.json.simple.parser.ParseException;

import application.model.Guest;

public class NFCCommunicationThread implements Runnable {
	
	private CardTerminal terminal;
	
	private Card currentCard;
	private ATR currentATR;
	
	private Guest currentGuest;
	
	private int status;
	
	// Sleeping time
	private int t;
	
	private String buffer;
	
	public static final int READY = 0;
	public static final int CONNECTED = 1;
	public static final int DISCONNECTED = 2;
	public static final int CANCELED = 3;
	private static final int t_step = 500;
	
	public NFCCommunicationThread() {
		status = READY;
		currentCard = null;
		t = t_step;
	}
	
	public Guest getCurrentGuest()
	{
		return currentGuest;
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		// Try connecting to the default terminal
		while(status != CONNECTED) {
			if(connectToDefaultTerminal() != CONNECTED) {
				try {
					Thread.sleep(t);
					System.out.println("No terminal available. Trying again in " + (t/1000) + " seconds...");
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					return;
				}
				t += t_step;
			}
		}
		
		t = t_step;
		
		while (status == CONNECTED) {
			try {
				String cardInfo = readCurrentCard();
				
				if(cardInfo == null) {
					System.out.println("Same card on reader. Waiting...");
					try {
						Thread.sleep(t);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					// Creating a guest from the info of the NFC card
					
					/*
					Guest g = Guest.newGuestFromJSONString(cardInfo);
					currentGuest = g;
					System.out.println(g.toString());*/
				}
				
			} catch (CardException e) {
				System.out.println("No card present or unable to read card... trying again");
				currentCard = null;
				currentATR = null;
				currentGuest = null;
				try {
					Thread.sleep(t);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
	}
	
	private String readCurrentCard() throws CardException {
		if(terminal.isCardPresent()) {
            //Select automatically protocol T=1
            String protocol = "T=1";
            
            Card newCard = terminal.connect(protocol);
            
            if(newCard.getATR().equals(currentATR)) {
            	return null;
            }
            
            currentCard = newCard;
            
            // Get the atr of the current card
            currentATR = currentCard.getATR();
            
            // Get the card channel
            CardChannel cc = currentCard.getBasicChannel();
  
            String cardInfo = readData(cc);
            
            return cardInfo;
		}
		
		throw new CardException("No card");
	}
	
	/**
	 * Connects to the default terminal
	 * Changes the status to either CONNECTED or DISCONNECTED
	 */
	private int connectToDefaultTerminal() {
		CardTerminal ct = _selectDefaultCardTerminal();
		if(ct != null) {
			terminal = ct;
			status = CONNECTED;
		} else {
			status = DISCONNECTED;
		}
		
		return status;
	}
	
	/**
	 * method selectDefaultCardTerminal
	 * Selects the default terminal among all available terminals and returns it
	 * @return
	 */
	private CardTerminal _selectDefaultCardTerminal() {
		TerminalFactory factory = TerminalFactory.getDefault();
        List<CardTerminal> terminals;
		try {
			terminals = factory.terminals().list();
			ListIterator<CardTerminal> terminalsIterator = terminals.listIterator();
			CardTerminal terminal = terminalsIterator.next();
			return terminal;
		} catch (CardException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return null;
   }
	
	private CardChannel getCardChannel() {
		try {
			while(!terminal.isCardPresent()) {
				terminal.waitForCardPresent(100);
				if(status == CANCELED) {
					return null;
				}
			}
			
			Card card = establishConnection();
			CardChannel cc = card.getBasicChannel();
			return cc;
		} catch (CardException e) {
			status = DISCONNECTED;
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Method establishConnection
	 * Connects with a car
	 * @param ct
	 * @return
	 */
    private Card establishConnection()
    {
            //Select automatically protocol T=1
            String p = "T=1";
            Card card = null;
            try 
            {
                    card = terminal.connect(p);
            } 
            catch (CardException e) 
            {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    return null;
            }
            ATR atr = card.getATR();
            
            return card;             
    }
	
	
	public String readData(CardChannel cc) throws CardException {
		buffer = "";

        String result = "";
        for(int i = 4; i < 35; i = i+2) {
        	if(_readPage(cc, i)) {
        		break;
        	}
        }

        result += buffer;
        
        return Utility.hexToASCII(buffer);
	}
	
	public boolean writeData(String data) throws CardException {
		CardChannel cc = getCardChannel();
		
		// Add the halt character to the string, and change it to a HEX string
		String d = Utility.asciiToHex(new String(data + "\\"));
		
		// Change the string to a byte array
        byte[] b = new BigInteger(d,16).toByteArray();
        
        // Split the array in chunks of 8 bytes
        byte[][] b_split = new byte[b.length/8 + 1][b.length];
        int j = 0;
        for(int i = 0; i < b.length; i=i+8) {
        	b_split[j] = Arrays.copyOfRange(b, i, i+8);
        	j++;
        }
        
        //Write all pages
        for(int i = 0; i < b_split.length; i++) {
        	_writePage(cc, 4+(2*i), b_split[i]);
        }

        terminal.waitForCardAbsent(0);
        
        return true;
	}
	
	/**
	 * Returns true if the page contains a halt character ("\")
	 * @param bufferint
	 * @return
	 * @throws CardException 
	 */
	private boolean _readPage(CardChannel cc, int offset) throws CardException {
        
        byte[] readCommand = {(byte) 0xFF, (byte) 0xB0, (byte) 0x00, (byte) offset, (byte) 0x08};
    	
    	CommandAPDU readData = new CommandAPDU(readCommand);
    	ResponseAPDU responseReadData = cc.transmit(readData);
    	
    	byte[] dataOut = responseReadData.getData();
    	
		StringBuilder sb = new StringBuilder();
		for(byte b : dataOut) {
			if(b == 92) {
				buffer += sb.toString();
				return true;
			}
			sb.append(String.format("%02X", b));
		}
    		
    	buffer += sb.toString();
        return false;
	}
	
	
	private void _writePage(CardChannel cc, int offset, byte[] dataOut) throws CardException {
        // Encode the length of the string in a byte
        byte bArraySize = new Integer(dataOut.length).byteValue();
        
        // Write command starting in page offset
        byte[] writeCommand = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) offset};
        
        // The byte array for the full command
        byte[] command = new byte[writeCommand.length + 1 + dataOut.length];
        for(int i = 0; i < writeCommand.length; i++) {
        	command[i] = writeCommand[i];
        }
        command[writeCommand.length] = bArraySize;
        for(int i = 0; i < dataOut.length; i++) {
        	command[i + writeCommand.length + 1] = dataOut[i];
        }
        
        CommandAPDU writeData = new CommandAPDU(command);
        ResponseAPDU responseWriteData = cc.transmit(writeData);	
	}
	
	


	public int getStatus() {
		// TODO Auto-generated method stub
		return status;
	}
	
	public void cancel() {
		status = CANCELED;
	}

}

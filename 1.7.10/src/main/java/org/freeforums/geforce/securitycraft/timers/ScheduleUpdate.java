package org.freeforums.geforce.securitycraft.timers;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import org.freeforums.geforce.securitycraft.blocks.BlockKeypad;
import org.freeforums.geforce.securitycraft.main.mod_SecurityCraft;

public class ScheduleUpdate{
		Timer timer;
		private int x;
		private int y;
		private int z;
		private int metadata;
		private int passcode;
		public ScheduleUpdate(int seconds, int par3, int par4, int par5, int par6){
			timer = new Timer();
			timer.schedule(new RemindTask(), seconds*1000); //TODO 60
			x = par3;
			y = par4;
			z = par5;
			passcode = par6;

			if(BlockKeypad.worldServerObj.getBlock(par3, par4, par5) == mod_SecurityCraft.Keypad){
				metadata = BlockKeypad.worldServerObj.getBlockMetadata(par3, par4 , par5);
				BlockKeypad.worldServerObj.setBlockMetadataWithNotify(par3, par4, par5, metadata + 5, 3);
				BlockKeypad.worldServerObj.notifyBlocksOfNeighborChange(par3, par4, par5, mod_SecurityCraft.Keypad);
			}
		}
		class RemindTask extends TimerTask{

			public void run(){
				if(BlockKeypad.worldServerObj.getBlock(x, y, z) == mod_SecurityCraft.Keypad){
					BlockKeypad.worldServerObj.setBlockMetadataWithNotify(x, y, z, metadata, 3);
					BlockKeypad.worldServerObj.notifyBlocksOfNeighborChange(x, y, z, mod_SecurityCraft.Keypad);
				}

				ByteArrayOutputStream BOS = new ByteArrayOutputStream(16);
				DataOutputStream outputStream = new DataOutputStream(BOS);
				
				try{
					outputStream.writeInt(x);
					outputStream.writeInt(y);
					outputStream.writeInt(z);
					outputStream.writeInt(passcode);
					
					

				}catch(IOException e){
					e.printStackTrace();
				}
				
				//TODO
				/*
				Packet250CustomPayload packet = new Packet250CustomPayload();
				packet.channel = "ReverseBlock";
				packet.data = BOS.toByteArray();
				packet.length = BOS.size();
				BlockKeypad.playerObj.sendQueue.addToSendQueue(packet);
				*/

				
				
				timer.cancel();
			}
		}
	}
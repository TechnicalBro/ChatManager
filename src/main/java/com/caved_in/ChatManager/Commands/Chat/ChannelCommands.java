package com.caved_in.chatmanager.commands.chat;

import com.caved_in.chatmanager.commands.CommandPermissions;
import com.caved_in.chatmanager.events.ChannelCreateEvent;
import com.caved_in.chatmanager.events.ChannelJoinEvent;
import com.caved_in.chatmanager.handlers.util.StringUtil;
import com.caved_in.chatmanager.events.ChannelDeleteEvent;
import com.caved_in.chatmanager.events.ChannelLeaveEvent;
import com.caved_in.chatmanager.handlers.player.PlayerHandler;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.caved_in.chatmanager.ChatManager;
import com.caved_in.chatmanager.commands.CommandController.CommandHandler;
import com.caved_in.chatmanager.commands.CommandController.SubCommandHandler;
import com.caved_in.chatmanager.events.handler.ChannelEventHandler;
import com.caved_in.chatmanager.handlers.chat.channels.ChannelInvitation;
import com.caved_in.chatmanager.handlers.chat.channels.ChatChannel;
import com.caved_in.chatmanager.handlers.helpmenu.HelpScreen;

public class ChannelCommands
{

	public ChannelCommands()
	{
	}

	@CommandHandler(name = "channel", usage = "/channel", permission = "multichat.channel")
	public void channelBaseCommand(CommandSender commandSender, String[] Args)
	{
		if (Args.length == 0)
		{
			commandSender.sendMessage(ChatColor.GOLD + "Please use the command '/channel help' for help.");
		}
	}

	@SubCommandHandler(name = "help", parent = "channel")
	public void channelHelpCommand(CommandSender commandSender, String[] commandArgs)
	{
		HelpScreen commandScreen = new HelpScreen("Channel Command Help");
		commandScreen.setHeader(ChatColor.AQUA + "<name> (Page <page> of <maxpage>)");
		commandScreen.setFlipColor(ChatColor.GREEN, ChatColor.DARK_GREEN);
		commandScreen.setEntry("/channel create <Name>", "Make a new channel with the given name");
		commandScreen.setEntry("/channel create private <Name>", "Make an Invite-Only channel with the given name");
		commandScreen.setEntry("/channel join <Channel Name>", "Used to join a chat channel.");
		commandScreen.setEntry("/channel leave", "Used to leave the current channel you're in.");
		commandScreen.setEntry("/channel delete <name>", "Used to delete a channel with the given name");
		commandScreen.setEntry("/channel invite <name>", "Invite a user to your private channel");
		commandScreen.setEntry("/channel accept", "Accept a channel invitation");
		commandScreen.setEntry("/channel deny", "Deny a channel invitation");
		commandScreen.setEntry("/channel list", "View a list of all the existing channels");
		commandScreen.setEntry("/channel reload", "Reloads the configuration file for MultiChat");
		commandScreen.setEntry("/channel save", "Saves MultiChat data to the configuration file");

		commandScreen.setFormat("<name> - <desc>");

		if (commandArgs.length == 1)
		{
			commandScreen.sendTo(commandSender, 1, 7);
		}
		else
		{
			if (commandArgs[1] != null && StringUtils.isNumeric(commandArgs[1]))
			{
				int Page = Integer.parseInt(commandArgs[1]);
				commandScreen.sendTo(commandSender, Page, 7);
			}
		}
	}

	@SubCommandHandler(name = "reload", parent = "channel", permission = "multichat.reload")
	public void channelReloadCommand(CommandSender commandSender, String[] commandArgs)
	{
		if (ChatManager.loadXmlConfig(true))
		{
			commandSender.sendMessage(StringUtil.formatColorCodes("&aMultiChat configuration has been reloaded"));
		}
		else
		{
			commandSender.sendMessage(StringUtil.formatColorCodes("&cThere was an error while reloading the configuration, please check the console for the reason"));
		}
	}

	@SubCommandHandler(name = "save", parent = "channel", permission = "multichat.save")
	public void channelSaveCommand(CommandSender commandSender, String[] commandArgs)
	{
		if (ChatManager.saveXmlConfig())
		{
			commandSender.sendMessage(StringUtil.formatColorCodes("&aMultiChat configuration has been saved"));
		}
	}

	@SubCommandHandler(name = "list", parent = "channel", permission = "multichat.channel.list")
	public void channelListCommand(CommandSender commandSender, String[] commandArgs)
	{
		HelpScreen channelList = new HelpScreen("Channels List");
		channelList.setHeader(StringUtil.formatColorCodes("&b<name> (Page <page> of <maxpage>)"));
		channelList.setSimpleColor(ChatColor.GOLD);
		channelList.setFormat("<name> <desc>");

		for(ChatChannel chatChannel : ChatManager.channelHandler.getChannels())
		{
			channelList.setEntry(chatChannel.getName(),StringUtil.formatColorCodes("has &c" + chatChannel.getChatMembers().size() + "&6 members in chat" + (chatChannel.isPrivate() ? ", &einvite only" : "") + (chatChannel.hasPermission() ? "&6, &brequires permission" : "")));
		}

		if (commandArgs.length == 1)
		{
			channelList.sendTo(commandSender, 1, 7);
		}
		else
		{
			if (commandArgs[1] != null && StringUtils.isNumeric(commandArgs[1]))
			{
				int pageNumber = Integer.parseInt(commandArgs[1]);
				channelList.sendTo(commandSender, pageNumber, 7);
			}
		}
	}

	@SubCommandHandler(name = "create", parent = "channel", permission = "multichat.channel.create")
//usage = "/addchannel <name>", permission = "vaeconnetwork.chat.message"
	public void channelCreateCommand(Player player, String[] commandArgs)
	{
		if (commandArgs.length > 1 && !commandArgs[1].isEmpty())
		{
			String channelName = commandArgs[1];
			if (!channelName.equalsIgnoreCase("private"))
			{
				if (commandArgs.length > 2)
				{
					channelName += " ";
					for(int I = 2; I < commandArgs.length; I++)
					{
						channelName += (I == (commandArgs.length - 1)) ? commandArgs[I] : commandArgs[I] + " ";
					}
				}

				if (!ChatManager.channelHandler.isChannel(channelName))
				{
					ChannelCreateEvent channelCreateEvent = new ChannelCreateEvent(new ChatChannel(channelName, "[" + channelName + "]"), player);
					ChannelEventHandler.handleChannelCreateEvent(channelCreateEvent);
				}
				else
				{
					player.sendMessage(StringUtil.formatColorCodes("&cThe channel &e" + channelName + "&c already exists"));
				}
			}
			else
			{
				if (player.hasPermission(CommandPermissions.CHANNEL_CREATEPRIVATE_PERMISSION))
				{
					if (commandArgs.length > 2 && !commandArgs[2].isEmpty())
					{
						String pChannelName = commandArgs[2];

						if (commandArgs.length > 3)
						{
							pChannelName += " ";
							for(int I = 3; I < commandArgs.length; I++)
							{
								pChannelName += (I == (commandArgs.length - 1)) ? commandArgs[I] : commandArgs[I] + " ";
							}
						}

						if (!ChatManager.channelHandler.isChannel(pChannelName))
						{
							ChatChannel privateChatChannel = new ChatChannel(pChannelName, ChatColor.GRAY + "[" + pChannelName + "]" + ChatColor.RESET, player.getName(), true);
							ChannelCreateEvent Event = new ChannelCreateEvent(privateChatChannel, player);
							ChannelEventHandler.handleChannelCreateEvent(Event);
						}
						else
						{
							player.sendMessage(StringUtil.formatColorCodes("&cThe channel &e" + channelName + "&c already exists"));
						}
					}
					else
					{
						player.sendMessage(StringUtil.formatColorCodes("&eTo create a private channel do &a/channel create private <Name>"));
					}
				}
				else
				{
					player.sendMessage(ChatColor.RED + "You do not have permission for this command");
				}
			}
		}
	}

	@SubCommandHandler(name = "delete", parent = "channel", permission = "multichat.channel.delete")
	public void deleteChannelCommand(Player player, String[] commandArgs)
	{
		if (commandArgs.length > 1 && !commandArgs[1].isEmpty())
		{
			String channelName = "";

			//Parse for channel name
			for(int I = 1; I < commandArgs.length; I++)
			{
				channelName += (I == (commandArgs.length - 1)) ? commandArgs[I] : commandArgs[I] + " ";
			}

			if (ChatManager.channelHandler.isChannel(channelName))
			{
				ChannelDeleteEvent channelDeleteEvent = new ChannelDeleteEvent(ChatManager.channelHandler.getChannel(channelName), player);
				ChannelEventHandler.handleChannelDeleteEvent(channelDeleteEvent);
			}
			else
			{
				player.sendMessage(StringUtil.formatColorCodes("&cThe Chat Channel &e" + channelName + "&c doesn't exist."));
			}
		}
	}

	@SubCommandHandler(name = "join", parent = "channel", permission = "multichat.channel.join")
	public void joinChannelEvent(Player player, String[] commandArgs)
	{
		if (commandArgs.length > 1 && !commandArgs[1].isEmpty())
		{
			String channelName = "";

			//Parse for channel name
			for(int I = 1; I < commandArgs.length; I++)
			{

				channelName += (I == (commandArgs.length - 1)) ? commandArgs[I] : commandArgs[I] + " ";
			}

			if (!ChatManager.channelHandler.isChannel(channelName))
			{
				player.sendMessage("Chat channel " + channelName + " doesn't exist");
			}
			else
			{
				ChannelJoinEvent channelJoinEvent = new ChannelJoinEvent(ChatManager.channelHandler.getChannel(channelName), player);
				ChannelEventHandler.handleChannelJoinEvent(channelJoinEvent);
			}
		}
		else
		{
			player.sendMessage(StringUtil.formatColorCodes("&cPlease include a channel to join"));
		}
	}

	@SubCommandHandler(name = "leave", parent = "channel", permission = "multichat.channel.join")
	public void leaveChannelCommand(Player player, String[] commandArgs)
	{
		ChannelLeaveEvent channelLeaveEvent = new ChannelLeaveEvent(ChatManager.channelHandler.getChannel(PlayerHandler.getData(player.getName()).getChatChannel()), player);
		ChannelEventHandler.handleChannelLeaveEvent(channelLeaveEvent);
	}

	@SubCommandHandler(name = "invite", parent = "channel", permission = "multichat.channel.invite")
	public void InviteChannel(Player player, String[] commandArgs)
	{
		String playerName = player.getName();
		String playerDisplayName = player.getDisplayName();
		if (commandArgs.length > 1 && !commandArgs[1].isEmpty())
		{
			String invitedPlayer = commandArgs[1];
			if (PlayerHandler.isOnline(invitedPlayer))
			{
				ChatChannel invitingChannel = ChatManager.channelHandler.getChannel(PlayerHandler.getData(playerName).getChatChannel());
				String invitingChannelName = invitingChannel.getName();
				if (!invitingChannelName.equalsIgnoreCase(ChatManager.GLOBAL_CHAT_CHANNEL))
				{
					if (ChatManager.channelHandler.addChannelInvitation(invitingChannelName, playerName, invitedPlayer))
					{
						PlayerHandler.getPlayer(invitedPlayer).sendMessage(StringUtil.formatColorCodes(String.format("&a%s&e has invited you to join their chat channel.", playerDisplayName)));
						PlayerHandler.getPlayer(invitedPlayer).sendMessage(StringUtil.formatColorCodes("&eTo accept, type &a/channel accept"));
						PlayerHandler.getPlayer(invitedPlayer).sendMessage(StringUtil.formatColorCodes("&eTo deny type &c/channel deny"));
						player.sendMessage(StringUtil.formatColorCodes(String.format("&a%s has been invited to join your channel.", playerDisplayName)));
					}
					else
					{
						player.sendMessage(StringUtil.formatColorCodes(String.format("&cFailed to invite &e%s&c to your channel.", invitedPlayer)));
					}
				}
				else
				{
					player.sendMessage(StringUtil.formatColorCodes("&cYou can't invite players to join the global chat channel."));
				}
			}
			else
			{
				player.sendMessage(StringUtil.formatColorCodes(String.format("&e%s &cis offline; &e%s &cneeds to be online to invite them to your channel", invitedPlayer)));
			}
		}
		else
		{
			player.sendMessage(StringUtil.formatColorCodes("&cThe proper usage is &e/channel invite <name>"));
		}
	}

	@SubCommandHandler(name = "accept", parent = "channel", permission = "multichat.channel.invite")
	public void acceptChannelInvitation(Player player, String[] commandArgs)
	{
		String playerName = player.getName();
		String playerDisplayName = player.getDisplayName();
		if (ChatManager.channelHandler.hasChannelInvitation(playerName))
		{
			ChannelInvitation channelInvitation = ChatManager.channelHandler.getInvitedChannel(playerName);
			String channelInviter = channelInvitation.getInvitingPlayer();
			if (PlayerHandler.isOnline(channelInviter))
			{
				if (ChatManager.channelHandler.acceptChannelInvitation(playerName))
				{
					player.sendMessage(StringUtil.formatColorCodes("&aYou've joined the chat channel!"));
					PlayerHandler.getPlayer(channelInviter).sendMessage(StringUtil.formatColorCodes(String.format("&a%s has accepted your channel invitation!", playerDisplayName)));
				}
				else
				{
					player.sendMessage(StringUtil.formatColorCodes("&eFailed to accept channel invitation; Did the invitation expire?"));
				}
			}
			else
			{
				player.sendMessage(StringUtil.formatColorCodes(String.format("&cThis invitation has expired; %s is offline.", channelInviter)));
				ChatManager.channelHandler.removeChannelInvitation(playerName);
			}
		}
		else
		{
			player.sendMessage(StringUtil.formatColorCodes("&cYou weren't invited to join any channels."));
		}
	}

	@SubCommandHandler(name = "deny", parent = "channel", permission = "multichat.channel.invite")
	public void denyInvitationCommand(Player player, String[] commandArgs)
	{
		String playerName = player.getName();
		String playerDisplayName = player.getDisplayName();
		if (ChatManager.channelHandler.hasChannelInvitation(player.getName()))
		{
			ChannelInvitation channelInvitation = ChatManager.channelHandler.getInvitedChannel(playerName);
			String channelInviter = channelInvitation.getInvitingPlayer();
			if (PlayerHandler.isOnline(channelInviter))
			{
				PlayerHandler.getPlayer(channelInviter).sendMessage(StringUtil.formatColorCodes(String.format("&e%s&c has declined your channel invitation.", playerDisplayName)));
			}
			ChatManager.channelHandler.removeChannelInvitation(playerName);
			player.sendMessage(StringUtil.formatColorCodes(String.format("&eYou've declined the channel invitation from &c%s", channelInviter)));
		}
		else
		{
			player.sendMessage(StringUtil.formatColorCodes("&cYou don't have any channel invitations."));
		}
	}
}

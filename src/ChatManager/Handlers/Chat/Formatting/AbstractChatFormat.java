package ChatManager.Handlers.Chat.Formatting;

import org.apache.commons.lang.StringUtils;

import ChatManager.Handlers.Chat.Formatting.Variables.ChatVariable;

public abstract class AbstractChatFormat implements IChatFormat
{
    private String Name = "";
    private String Format = "";
    private ChatVariable MessageVariable = new ChatVariable("%message%",true,"%$2s");
    private ChatVariable SenderVariable = new ChatVariable("%sender%",true,"%$1s");
    
    public AbstractChatFormat(String Name, String Format)
    {
        this.Name = Name;
        this.setFormat(Format);
    }
    
    @Override
    public String getName()
    {
        return this.Name;
    }
    
    @Override
    public String getFormat()
    {
        return this.Format;
    }
    
    @Override
    public String getFormattedMessage(String Sender, String Message)
    {
        return StringUtils.replace(StringUtils.replaceOnce(this.Format, "%$1s", Sender), "%$2s", Message);
    }
    
    @Override
    public void setFormat(String Format)
    {
	    this.Format = StringUtils.replaceOnce(StringUtils.replaceOnce(Format,SenderVariable.getVariable(), SenderVariable.getReplaces()),MessageVariable.getVariable(),MessageVariable.getReplaces());
    }
}

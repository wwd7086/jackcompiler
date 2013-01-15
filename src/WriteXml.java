import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
public class WriteXml 
{
	File target;
	FileWriter writer;
	BufferedWriter bwriter;
	int count=0;
	WriteXml(File target)
	{
		this.target=target;
		try 
		{
			writer=new FileWriter(target);
			bwriter=new BufferedWriter(writer);
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	void WriteStart(String name)
	{
		try {
			dospace();
			bwriter.write("<"+name+">"); bwriter.newLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		count++;
	}
	
	void WriteString(String name, String value)
	{
		value=value.replace("&", "&amp;");
		value=value.replace("<", "&lt;");
		value=value.replace(">", "&gt;");
		try {
			dospace();
			bwriter.write("<"+name+"> "+value+" </"+name+">");bwriter.newLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	void WriteEnd(String name)
	{
		count--;
		try {
			dospace();
			bwriter.write("</"+name+">");bwriter.newLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	void dospace()
	{
		try
		{
			for(int i=0;i<count;i++)
			{
				bwriter.write("  ");
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	void closeWrite()
	{
		try {
			bwriter.close();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}

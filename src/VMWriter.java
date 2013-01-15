import java.io.*;

public class VMWriter 
{
	File outVM;
	FileWriter vmWriter;
	BufferedWriter vmBuf;
	VMWriter(File outVM)
	{
		this.outVM=outVM;
		try 
		{
			vmWriter=new FileWriter(outVM);
			vmBuf=new BufferedWriter(vmWriter);
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	void writePush(String segement, int index)
	{
		try 
		{
			vmBuf.write("push "+segement+" "+index);
			vmBuf.newLine();
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	void writePop(String segement, int index)
	{
		try 
		{
			vmBuf.write("pop "+segement+" "+index);
			vmBuf.newLine();
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	void writeArithmetic(String command)
	{
		try 
		{
			vmBuf.write(command);
			vmBuf.newLine();
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	void writeLabel(String label)
	{
		try 
		{
			vmBuf.write("label "+label);
			vmBuf.newLine();
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	void writeGoto(String label)
	{
		try 
		{
			vmBuf.write("goto "+label);
			vmBuf.newLine();
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	void writeIf(String label)
	{
		try 
		{
			vmBuf.write("if-goto "+label);
			vmBuf.newLine();
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	void writeCall(String name, int nArgs)
	{
		try 
		{
			vmBuf.write("call "+name+" "+nArgs);
			vmBuf.newLine();
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	void writeFunction(String name, int nLocals)
	{
		try 
		{
			vmBuf.write("function "+name+" "+nLocals);
			vmBuf.newLine();
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	void writeReturn()
	{
		try 
		{
			vmBuf.write("return");
			vmBuf.newLine();
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	void close()
	{
		try
		{
			vmBuf.close();
			vmWriter.close();	
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

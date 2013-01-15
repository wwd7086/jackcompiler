import java.io.*;
import java.util.Scanner;
public class JackAnalyzer 
{
	public static void main(String[] Args)
	{
		//////////////////////////////init input file//////////////////////////////////
		String target="";
		boolean isExist=false;
		boolean isJack=false;
		File targetFile=new File("");
		File[] compileFile=new File[0];
		File outXML;
		File outVM;
		Scanner inTarget=new Scanner(System.in);
		
		while(!isJack)
		{
			System.out.println("input the jack file you want compile:");
			while(!isExist)
			{
				if(inTarget.hasNextLine())
				{
					target=inTarget.nextLine();
				}
				targetFile=new File(target);
				isExist=targetFile.exists();
				if(!isExist)
					System.out.println("can not find file or directory input again:");
			}
			isExist=false;
		
			if(targetFile.isDirectory())
			{
				compileFile=targetFile.listFiles( new FilenameFilter()
					         {public boolean accept(File arg0, String arg1)
						     	{ if(arg1.matches(".*\\.jack")) return true;
						     	  else return false; }});
				if(compileFile.length==0)
					System.out.println("there is no .jack file");
				else
				{
					System.out.println("number of files need to be compile:"+compileFile.length);
					isJack=true;
				}
			}
			else if(targetFile.getName().matches(".*\\.jack"))
			{
				compileFile=new File[1];
				compileFile[0]=targetFile;
				System.out.println("number of files need to be compile:"+compileFile.length);
				isJack=true;
			}
			else
				System.out.println("there is no .jack file");
		}
		
		
		/////////////////init compile////////////////////////////////
		for(int i=0;i<compileFile.length;i++)
		{
			outXML=new File(compileFile[i].getParent(),compileFile[i].getName().replace(".jack", ".xml"));
			outVM=new File(compileFile[i].getParent(),compileFile[i].getName().replace(".jack", ".vm"));
			CompilationEngine cpe=new CompilationEngine(compileFile[i],outXML,outVM);
			cpe.CompileClass();
			cpe.CompileComplete();
			System.out.println((i+1)+":compile complete");
		}
	}
}

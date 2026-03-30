
package mlssdd.codesmells.detection.repository;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import mlssdd.codesmells.detection.AbstractCodeSmellDetection;
import mlssdd.codesmells.detection.ICodeSmellDetection;
import mlssdd.kernel.impl.MLSCodeSmell;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.lang.*;
import java.util.*;

public class UnusedDeclarationDetectionModified extends AbstractCodeSmellDetection
		implements ICodeSmellDetection {

	public void detect(final Document xml) {

		 XPathFactory xpathfactory = XPathFactory.newInstance();
    XPath xpath = xpathfactory.newXPath();

		try {
			final NodeList javaList =
				(NodeList) AbstractCodeSmellDetection.JAVA_FILES_EXP
					.evaluate(xml, XPathConstants.NODESET);
			final NodeList cList =
				(NodeList) AbstractCodeSmellDetection.C_FILES_EXP
					.evaluate(xml, XPathConstants.NODESET);
			final int javaLength = javaList.getLength();
			final int cLength = cList.getLength();

			final Map<String, MLSCodeSmell> resultMap = new HashMap<>();

			//modification
			//C files
		    XPathExpression cn = xpath.compile("//unit[@language='C'] | //unit[@language='C++']");
		    NodeList Cfiles = (NodeList) cn.evaluate(xml, XPathConstants.NODESET);

		    //list of native methods with static linking
		    final Set<String> cFunctions = new HashSet<>();

		    //list of registered native methods (dynamic linking)
		     List<String> nativeRegisteredList = new ArrayList<String>();


		    for (int c=0; c<Cfiles.getLength();c++)
		    {
		      //dynamic linking of native methods.
		      XPathExpression x = xpath.compile("descendant::decl_stmt/decl[type/name='JNINativeMethod']/init/expr/block/expr/block |" +
		      	"descendant::decl_stmt/decl[type/name='JNINativeMethod']/init/expr/block/expr/call |" +
		      	"descendant::decl_stmt/decl[type/name='JNINativeMethod']/init/expr/block/macro |" +
		      	"descendant::decl_stmt/decl[type/name='JNINativeMethod']/argument_list/argument");
		      NodeList registerBlocks = (NodeList) x.evaluate(Cfiles.item(c), XPathConstants.NODESET);

		      for(int r=0; r<registerBlocks.getLength(); r++)
		      {
		        XPathExpression m = xpath.compile("(descendant::expr/literal)[1] | (descendant::expr/block/expr[1]/literal) | argument_list/argument");
		        NodeList methods = (NodeList) m.evaluate(registerBlocks.item(r), XPathConstants.NODESET);
		        for( int met=0; met<methods.getLength(); met++)
		        {
		          //nativeImplemented+=1;
		          nativeRegisteredList.add(methods.item(met).getTextContent().replace("\"",""));
		          //System.out.println(methods.item(met).getTextContent());  
		        }
		        
		      }

		      //static linking of native methods
		    	XPathExpression func = xpath.compile("descendant::function/name");
		    	NodeList functions = (NodeList) func.evaluate(Cfiles.item(c), XPathConstants.NODESET);

		    	for(int fun=0; fun<functions.getLength(); fun++)
		    	{
		    		cFunctions.add(functions.item(fun).getTextContent());
		        //System.out.println(functions.item(fun).getTextContent());
		    	}

		      //if the implementation is done by macros eg.-java-smt-master
		      //extract the definition of the macro whose value contains JNIEXPORT
		      //eg- <cpp:define>#<cpp:directive>define</cpp:directive>
		      // <cpp:macro><name>DEFINE_FUNC</name>
		      //<parameter_list>(<parameter><type><name>jreturn</name></type></parameter>, 
		      //<parameter><type><name>func_escaped</name></type></parameter>)</parameter_list>
		      //</cpp:macro> \
		      //<cpp:value>JNIEXPORT j##jreturn JNICALL 
		      //Java_org_sosy_1lab_java_1smt_solvers_mathsat5_Mathsat5NativeApi_msat_##func_escaped</cpp:value></cpp:define>
		      
		      XPathExpression xp = xpath.compile("descendant::*[name()='cpp:define'][contains(./*[name()='cpp:value'], 'JNICALL')]");
		      NodeList macros = (NodeList) xp.evaluate(Cfiles.item(c), XPathConstants.NODESET);
		      for(int mc=0; mc<macros.getLength();mc++)
		      {
		        //name of the macro
		        XPathExpression xp2 = xpath.compile("descendant::*[name()='cpp:macro']/name");
		        String macroName = xp2.evaluate(macros.item(mc));
		        System.out.println(macroName);

		        //extract the name of first part of the name of the implemeted method which will be inside 
		        // cpp:value tag splitted by space starts at 3rd index then again splitted by # 
		        XPathExpression fn1 = xpath.compile("descendant::*[name()='cpp:value']");
		        String funcPart1 = fn1.evaluate(macros.item(mc));
		        funcPart1 = funcPart1.split(" ")[3].split("#")[0];

		        //get the arguments of the each macroName macro that will be the second part of the implemented method name
		        //XPathExpression n2 = xpath.compile("//name[text() = 'DEFINE_FUNC']/following-sibling::argument_list[1]/argument[2]");
		        String n2 = String.format("//name[text()='%s']/following-sibling::argument_list[1]/argument",macroName);
		        NodeList defineFuncs = (NodeList) xpath.evaluate(n2, xml, XPathConstants.NODESET);
		        
		        for(int df=0; df<defineFuncs.getLength(); df++)
		        {
		          
		          String funcPart2 = defineFuncs.item(df).getTextContent();
		          cFunctions.add(funcPart1+funcPart2);
		          //System.out.println(funcPart1+defineFuncs.item(df).getTextContent());
		        }


		      }



		    }



			for (int i = 0; i < javaLength; i++) {
				final Node javaFile = javaList.item(i);
				final String javaFilePath =
					AbstractCodeSmellDetection.FILEPATH_EXP.evaluate(javaFile);
				// final NodeList declList =
				// 	(NodeList) AbstractCodeSmellDetection.NATIVE_DECL_EXP
				// 		.evaluate(javaFile, XPathConstants.NODESET);
				XPathExpression e5 = xpath.compile("descendant::function_decl[type/specifier='native']/name");
      			final NodeList declList = (NodeList) e5.evaluate(javaFile, XPathConstants.NODESET);
				final int declLength = declList.getLength();
				//System.out.println(declLength);
				for (int j = 0; j < declLength; j++) {
					boolean implemented= false;
					final Node thisDecl = declList.item(j);
					final String thisNativeFunction = thisDecl.getTextContent();
					System.out.println(thisNativeFunction);
					
					//class
		        	XPathExpression cn1 = xpath.compile("(ancestor::class)[last()]");
		        	NodeList thisClass = (NodeList) cn1.evaluate(thisDecl, XPathConstants.NODESET);
		        	//System.out.println(thisClass.item(0).getTextContent());

		        	//class name
		        	XPathExpression cn2 = xpath.compile("name/name | name");
		        	String className = cn2.evaluate(thisClass.item(0));
		          	className = className.split("<")[0];
		        	System.out.println(className);

		        	//fullname
		        	String fullName;
		          String thisNativeMethodModified=thisNativeFunction.replace("_","_1");
		          String classNameModified = className.replace("_","_1");
		          
		          fullName = classNameModified+"_"+thisNativeMethodModified;
		          System.out.println(fullName);
		          	//Iterator name = cFunctions.iterator();
		          	//while(name.hasNext())
		          	for (String name : cFunctions)
		          	{
		              if(name.contains(fullName) | nativeRegisteredList.contains(thisNativeFunction))
		              {
		                implemented=true;
		              }
		            }
		          	if(!implemented)
		          	{
		          		final String thisClass1 = className;
						final String thisPackage =
							AbstractCodeSmellDetection.PACKAGE_EXP
								.evaluate(thisDecl);
						resultMap
							.put(
								thisNativeFunction,
								new MLSCodeSmell(
									this.getCodeSmellName(),
									"",
									thisNativeFunction,
									thisClass1,
									thisPackage,
									javaFilePath));
		          	}
					
				}
			}
			
			this.setSetOfSmells(new HashSet<>(resultMap.values()));
		}
		catch (final XPathExpressionException e) {
			e.printStackTrace();
		}
	}

}

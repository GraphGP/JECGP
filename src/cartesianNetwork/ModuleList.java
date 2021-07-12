package cartesianNetwork;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * The List that contains all the modules of the Individual
 * @author Björn Piepenbrink
 *
 */
public class ModuleList  implements Serializable {
	
	private static final long serialVersionUID = 1L;
	//Module List consisting of all addedModules
	ArrayList<Module> moduleList;
	
	/**
	 * creates a new empty ModuleList
	 */
	public ModuleList(){
		moduleList = new ArrayList<>();
	}
	
	/**
	 * creates a ModuleList with the given moduleList
	 * @param moduleList the List of Modules that should be used
	 */
	public ModuleList(ArrayList<Module> moduleList){
		this.moduleList = moduleList;
	}
	
	public ArrayList<Module> getModuleList(){
		return moduleList;
	}
	
	public int getNrOfModules(){
		return moduleList.size();
	}
	
	/**
	 * Returns an unused Identifier of the ModuleList
	 * @return the unused Identifier
	 */
	public int getFirstUnusedIdentifier(){
		int identifier = 0;
		boolean used = identifierUsed(identifier);
		while(used){
			identifier++;
			used = identifierUsed(identifier);
		}
		return identifier;
	}
	
	private boolean identifierUsed(int id){
		for(Module module : moduleList){
			if(module.getIdentifier()==id){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Get a Module with its identifier
	 * @param identifier The Identifier of the Module
	 * @return The Module with the given identifier
	 */
	public Module getModuleWithIdentifier(int identifier){
		for(Module module : moduleList){
			if(module.getIdentifier()==identifier){
				return module;
			}
		}
		throw new IllegalArgumentException("identifier is not used by any Module");
	}
	
	/**
	 * Deletes the Module with the given Identifier
	 * @param identifier the identifier of the module that should be deleted
	 */
	public void deleteModuleWithIdentifier(int identifier){
		for(int i=0;i<moduleList.size();i++){
			if(moduleList.get(i).getIdentifier()==identifier){
				moduleList.remove(i);
				return;
			}
		}
	}
	
	/**
	 * Adds the given Module to the Module List
	 * @param module The Module that hould be added
	 * @throws Exception if the identifier is already in use by another Module
	 * 	(use getFirstUnusedIdentifier first!)
	 */
	public void addModule(Module module) throws Exception{
		if(identifierUsed(module.getIdentifier()))throw new Exception("Identifier already used in ModuleList");
		moduleList.add(module);
	}
	
	/**
	 * copies the Modulelist
	 * @return an identical Copy of the Modulelist
	 */
	public ModuleList copy(){
		ArrayList<Module> newModuleList = new ArrayList<>();
		for(Module m : moduleList){
			newModuleList.add(m.copy());
		}
		return new ModuleList(newModuleList);
	}
}

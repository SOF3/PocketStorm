package io.pmmp.pocketstorm.inspections;

import com.intellij.codeInspection.InspectionToolProvider;
import org.jetbrains.annotations.NotNull;

import io.pmmp.pocketstorm.inspections.perm.PermissionExistenceInspection;

public class InspectionsProvider implements InspectionToolProvider{
	@NotNull
	@Override
	public Class[] getInspectionClasses(){
		return new Class[]{
				PermissionExistenceInspection.class,
		};
	}
}

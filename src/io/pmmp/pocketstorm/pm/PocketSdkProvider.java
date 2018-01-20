package io.pmmp.pocketstorm.pm;

import java.util.Collection;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.AdditionalLibraryRootsProvider;
import com.intellij.openapi.roots.SyntheticLibrary;
import org.jetbrains.annotations.NotNull;

public class PocketSdkProvider extends AdditionalLibraryRootsProvider{
	@NotNull
	@Override
	public Collection<SyntheticLibrary> getAdditionalProjectLibraries(@NotNull Project project){
		return super.getAdditionalProjectLibraries(project);
	}
}

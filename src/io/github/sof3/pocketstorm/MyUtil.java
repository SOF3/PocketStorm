package io.github.sof3.pocketstorm;

import java.io.IOException;

import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public final class MyUtil{
	@NotNull
	public static VirtualFile lazyCreateChildDir(Object requester, VirtualFile file, String path) throws IOException{
		return lazyCreateChildDir(requester, file, path.split("[\\\\/]+"));
	}

	@NotNull
	public static VirtualFile lazyCreateChildDir(Object requester, VirtualFile file, String[] path) throws IOException{
		for(String name : path){
			VirtualFile child;
			if((child = file.findChild(name)) != null){
				file = child;
			}else{
				file = file.createChildDirectory(requester, name);
			}
		}
		return file;
	}
}

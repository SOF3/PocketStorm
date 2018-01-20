package io.pmmp.pocketstorm.util;

import java.io.File;
import java.io.IOException;
import java.util.List;

import lombok.Getter;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import org.jetbrains.annotations.NotNull;

public final class MyFileUtil{
	@Getter(lazy = true) private final static File configPath = computeConfigPath();

	@NotNull
	private static File computeConfigPath(){
		File file = new File(PathManager.getConfigPath(), "PocketStorm");
		if(!file.isDirectory() && !file.mkdirs()){
			throw new RuntimeException("Cannot mkdir " + file);
		}
		return file;
	}

	public static boolean isIn(VirtualFile parent, VirtualFile child){
		do{
			if(parent.equals(child)){
				return true;
			}
		}while((child = child.getParent()) != null);
		return false;
	}

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

	public static List<VirtualFile> findFile(@NotNull List<VirtualFile> list, @NotNull VirtualFile file, @NotNull String name){
		VfsUtilCore.visitChildrenRecursively(file, new VirtualFileVisitor(){
			@Override
			public boolean visitFile(@NotNull VirtualFile file){
				if(".idea".equals(file.getName())){
					return false;
				}
				if(file.isDirectory()){
					return true;
				}
				if(name.equals(file.getName())){
					list.add(file);
				}
				return false;
			}
		});
		return list;
	}
}

package io.pmmp.pocketstorm.pm;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import lombok.SneakyThrows;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.io.HttpRequests;
import org.jetbrains.annotations.Nullable;

import io.pmmp.pocketstorm.util.ResultRunnable;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.errors.GitAPIException;

public class PocketSdkManager{

	private final static PocketSdkManager ourInstance = new PocketSdkManager();

	public static PocketSdkManager getInstance(){
		return ourInstance;
	}

	private File sdkDir;

	private PocketSdkManager(){
		sdkDir = new File(PathManager.getSystemPath(), "pm_sdk");
		if(!sdkDir.isDirectory() && !sdkDir.mkdirs()){
			throw new RuntimeException("Cannot mkdirs " + sdkDir);
		}
	}

	public boolean hasSdk(String version){
		return new File(sdkDir, version + ".phar").isFile();
	}

	@Nullable
	public File getSdk(Project project, String version){
		File file = new File(sdkDir, version + ".phar");
		if(!file.isFile()){
			boolean complete = ProgressManager.getInstance().runProcessWithProgressSynchronously(new ResultRunnable<Boolean>(){
				@Override
				public Boolean execute(){
					ApiVersion api = PocketMine.apiList.waitSync().get(version);
					if(api == null || api.getPharLink() == null){
						return false;
					}
					ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
					try{
						HttpRequests.request(api.getPharLink()).saveToFile(file, indicator);
						return true;
					}catch(IOException e){
						e.printStackTrace();
						return false;
					}
				}
			}, "Downloading PocketMine-MP.phar for API " + version, true, project);
			if(!complete){
				return null;
			}
		}
		return file;
	}

	public File getDevSdk(){
		return getDevSdk("master");
	}

	@SneakyThrows({IOException.class, GitAPIException.class})
	public File getDevSdk(String ref){
		File root = new File(sdkDir, "clone");
		Git repo;
		boolean clone = !root.isDirectory();
		if(clone){
			repo = Git.cloneRepository().setURI("https://github.com/pmmp/PocketMine-MP.git").setDirectory(root).setCloneSubmodules(true).call();
		}else{
			repo = Git.open(root);
		}
		repo.checkout().setName(ref).call();
		if(fileStartsWith(new File(root, ".git/HEAD"), "ref: refs/heads/".getBytes())){
			repo.pull().setFastForward(MergeCommand.FastForwardMode.FF_ONLY).call();
		}
		repo.submoduleUpdate().call();
		// TODO composer install
		return root;
	}

	private static boolean fileStartsWith(File file, byte[] bytes){
		if(!file.isFile()){
			return false;
		}
		try(
				InputStream is = new FileInputStream(file)
		){
			byte[] buffer = new byte[bytes.length];
			int length = is.read(buffer);
			return length == bytes.length && Arrays.equals(bytes, buffer);
		}catch(IOException e){
			return false;
		}
	}
}

package io.pmmp.pocketstorm.project;

import java.util.Set;

import lombok.Value;

@Value
public class DummyCommand{
	String name;
	String description;
	String usage;
	Set<String> aliases;
	String permission;
}

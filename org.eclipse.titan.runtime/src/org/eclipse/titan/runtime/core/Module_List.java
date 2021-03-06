/******************************************************************************
 * Copyright (c) 2000-2019 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.text.MessageFormat;
import java.util.ArrayList;

import org.eclipse.titan.runtime.core.TTCN_Logger.Severity;

/**
 * The base class of handling modules
 *
 * @author Kristof Szabados
 */
public final class Module_List {
	public static ArrayList<TTCN_Module> modules = new ArrayList<TTCN_Module>();

	private Module_List() {
		// private constructor to disable accidental instantiation
	}

	public static void add_module(final TTCN_Module module) {
		modules.add(module);
	}

	public static void remove_module(final TTCN_Module module) {
		modules.remove(module);
	}

	//FIXME implement single_control_part
	public static TTCN_Module lookup_module(final String module_name) {
		for (final TTCN_Module module : modules) {
			if (module.module_name.equals(module_name)) {
				return module;
			}
		}

		return null;
	}

	public static void pre_init_modules() {
		for (final TTCN_Module module: modules) {
			module.pre_init_module();
		}
	}

	public static void pre_init_module(final String module_name) {
		final TTCN_Module module = lookup_module(module_name);
		module.pre_init_module();
	}

	public static void post_init_modules() {
		for (final TTCN_Module module: modules) {
			module.post_init_module();
		}
	}

	public static void post_init_module(final String module_name) {
		final TTCN_Module module = lookup_module(module_name);
		module.post_init_module();
	}

	
	public static void start_function(final String module_name, final String function_name, final Text_Buf function_arguments) {
		final TTCN_Module module = lookup_module(module_name);
		if (module == null){
			function_arguments.cut_message();

			throw new TtcnError(MessageFormat.format("Internal error: Module {0} does not exist.", module_name));
		}

		module.start_ptc_function(function_name, function_arguments);
	}

	//originally Module_List::initialize_component
	public static void initialize_component(final String module_name, final String component_type, final boolean init_base_comps) {
		final TTCN_Module module = lookup_module(module_name);
		if (module == null) {
			throw new TtcnError(MessageFormat.format("Internal error: Module {0} does not exist.", module_name));
		} else if (!module.init_comp_type(component_type, init_base_comps)) {
			throw new TtcnError(MessageFormat.format("Internal error: Component type {0} does not exist in module {1}.", component_type, module_name));
		}
	}

	//originally Module_List::initialize_system_port
	static void initialize_system_port(final String module_name, final String component_type,final String port_name)
	{
		final TTCN_Module system_module = lookup_module(module_name);
		if (system_module == null) {
			throw new TtcnError(MessageFormat.format("Internal error: Module {0} does not exist.", module_name));
		} else if (!system_module.init_system_port(component_type, port_name)) {
			throw new TtcnError(MessageFormat.format("Internal error: Cannot find port {0} in component type {1}, or component type {2} in module {3}.", port_name, component_type, component_type, module_name));
		}
	}

	public static void set_param(final Param_Types.Module_Parameter param) {
		// The first segment in the parameter name can either be the module name,
		// or the module parameter name - both must be checked

		final String first_name = param.get_id().get_current_name();
		String second_name = null;
		boolean param_found = false;

		// Check if the first name segment is an existing module name
		final TTCN_Module module = lookup_module(first_name);
		if (module != null && param.get_id().next_name()) {
			param_found = module.set_module_param(param);
			if (!param_found) {
				second_name = param.get_id().get_current_name(); // for error messages
			}
		}

		// If not found, check if the first name segment was the module parameter name
		// (even if it matched a module name)
		if (!param_found) {
			param.get_id().reset(); // set the position back to the first segment
			for (final TTCN_Module module2 : modules) {
				if (module2.set_module_param(param)) {
					param_found = true;
				}
			}
		}

		// Still not found -> error
		if (!param_found) {
			if (module == null) {
				param.error(MessageFormat.format("Module parameter cannot be set, because module `{0}'' does not exist, and no parameter with name `{0}'' exists in any module.", first_name));
			} else if (!module.has_set_module_param()) {
				param.error(MessageFormat.format("Module parameter cannot be set, because module `{0}'' does not have parameters, and no parameter with name `{0}'' exists in other modules.", first_name));
			} else {
				param.error(MessageFormat.format("Module parameter cannot be set, because no parameter with name `{0}'' exists in module `{1}'', and no parameter with name `{1}'' exists in any module.", second_name, first_name));
			}
		}
	}

	public static void execute_control(final String module_name) {
		final TTCN_Module module = lookup_module(module_name);
		if (module == null) {
			throw new TtcnError(MessageFormat.format("Module {0} does not exist.", module_name));
		} else {
			try {
				module.control();
			} catch (TtcnError TC_error) {
				TTCN_Logger.log_str(Severity.ERROR_UNQUALIFIED, MessageFormat.format("Unrecoverable error in control part of module {0}. Execution aborted.", module_name));
			} catch (TC_End TC_end) {
				TTCN_Logger.log_str(Severity.FUNCTION_UNQUALIFIED, MessageFormat.format("Control part of module {0} was stopped.", module_name));
			}
		}
	}

	public static void execute_testcase(final String module_name, final String testcase_name) {
		final TTCN_Module module = lookup_module(module_name);
		if (module == null) {
			throw new TtcnError(MessageFormat.format("Module {0} does not exist.", module_name));
		} else {
			module.execute_testcase(testcase_name);
		}
	}

	public static void execute_all_testcases(final String module_name) {
		final TTCN_Module module = lookup_module(module_name);
		if (module == null) {
			throw new TtcnError(MessageFormat.format("Module {0} does not exist.", module_name));
		} else {
			module.execute_all_testcases();
		}
	}

	//FIXME implement print version

	public static void send_usage_stats() {
		//FIXME collect version info
		StringBuilder builder = new StringBuilder("module version infos");
		//FIXME add module versions
		Usage_Stats.sendAsync(builder.toString());
	}

	public static void clean_up_usage_stats() {
		
	}

	public void list_modulepars() {
		for (final TTCN_Module module: modules) {
			module.list_modulepars();
		}
	}

	//FIXME implement list_testcases
	public static void push_version(final Text_Buf text_buf) {
		text_buf.push_int(modules.size());
		for (final TTCN_Module module: modules) {
			module.push_version(text_buf);
		}
	}
}

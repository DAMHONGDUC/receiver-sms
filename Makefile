# Short commands for this project; the targets come from the script-tools submodule. Run `make` to list them.
SCRIPT_TOOLS := packages/script-tools
VARIANT := DevDebug

ifeq ($(wildcard $(SCRIPT_TOOLS)/android/android.mk),)
$(error $(SCRIPT_TOOLS) is empty: run git submodule update --init)
endif
include $(SCRIPT_TOOLS)/android/android.mk

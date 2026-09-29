# Project commands. Android targets (build, install, test, lint, apk, aab, sms, clean) come from
# packages/script-tools/android/android.mk; `make` lists them all. FLAVOR=<flavor> picks a release flavor.

# Relative to the project root: make cannot handle spaces in paths.
SCRIPT_TOOLS := packages/script-tools
# Variant for build/install/test/lint.
VARIANT := DevDebug

.PHONY: setup tools-update release
# Release builds share Gradle outputs, so they must not run in parallel.
.NOTPARALLEL:

# Before `make setup` the submodule is empty, so only setup and a hint are available.
ifeq ($(wildcard $(SCRIPT_TOOLS)/android/android.mk),)
.DEFAULT_GOAL := help
help:
	@echo "$(SCRIPT_TOOLS) is empty; run make setup first."
else
include $(SCRIPT_TOOLS)/android/android.mk
endif

setup: ## Fetch the script-tools submodule after cloning
	git submodule update --init --recursive

tools-update: ## Pull the latest script-tools; commit the new submodule pointer afterwards
	git submodule update --init --remote $(SCRIPT_TOOLS)

release: aab apk ## Signed release AAB and APK into Release/

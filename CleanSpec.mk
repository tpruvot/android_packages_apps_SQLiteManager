# Automatically deleted after a reposync change, on next build

$(call add-clean-step, rm -rf $(OUT_DIR)/target/common/obj/APPS/SQLiteManager_intermediates)
$(call add-clean-step, rm -rf $(PRODUCT_OUT)/system/app/SQLiteManager.apk)
$(call add-clean-step, rm -rf $(PRODUCT_OUT)/data/app/SQLiteManager.apk)


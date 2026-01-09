package com.iviet.ivshs.service;

import com.iviet.ivshs.dto.HomeViewModel;

public interface HomeViewService {
	
	HomeViewModel getModel();
	void refreshDashboardData();
}

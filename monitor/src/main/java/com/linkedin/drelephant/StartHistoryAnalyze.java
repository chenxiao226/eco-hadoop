package com.linkedin.drelephant;

import org.hit.monitor.service.impl.YarnAppResultServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

import com.linkedin.drelephant.DrElephant;
 
@Service
public class StartHistoryAnalyze implements ApplicationListener<ContextRefreshedEvent>
{
	@Autowired
	private DrElephant drElephant;
	
	@Autowired
	private YarnAppResultServiceImpl yarnAppResultServiceImpl;
 
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event)
    {
        if(event.getApplicationContext().getParent() == null)//root application context 没有parent，他就是老大.
        {
             //需要执行的逻辑代码，当spring容器初始化完成后就会执行该方法。
        }  
 
        //或者下面这种方式,只加载一次
        if(event.getApplicationContext().getDisplayName().equals("Root WebApplicationContext"))
        {
        	StartHistoryAnalyzeTime.MR_Last_currentTime=yarnAppResultServiceImpl.getLastTime();
        	
            new Thread(drElephant).start();
            //设置开始分析的时间
            
        }
    }
}
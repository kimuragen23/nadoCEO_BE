import React, { useState } from 'react';
import { MainChatPanel } from './MainChatPanel';
import { SubChatPanel } from './SubChatPanel';
import { LearningPathBar } from './LearningPathBar';
import { Sidebar } from './Sidebar';
import { LogOut, ChevronDown, User, Menu } from 'lucide-react';
import { Button } from '../ui/button';
import { Avatar, AvatarFallback } from '../ui/avatar';
import { Separator } from '../ui/separator';
import { Sheet, SheetContent, SheetTrigger } from '../ui/sheet';
import { TooltipProvider } from '../ui/tooltip';
import { useAuthStore } from '../../store/authStore';
import keycloak from '../../auth/keycloak';

export function DualChatLayout() {
  const [isSidebarCollapsed, setIsSidebarCollapsed] = useState(false);
  const { userName, authenticated } = useAuthStore();

  const handleLogout = () => {
    keycloak.logout({ redirectUri: window.location.origin });
  };

  const displayName = userName || '사용자';

  return (
    <TooltipProvider>
      <div className="flex h-screen bg-[#F4F5F0] text-slate-900 overflow-hidden font-sans">
        {/* Desktop Sidebar */}
        <div className="hidden lg:block">
          <Sidebar
            isCollapsed={isSidebarCollapsed}
            onToggle={() => setIsSidebarCollapsed(!isSidebarCollapsed)}
          />
        </div>

        {/* Main Content Area */}
        <div className="flex-1 flex flex-col min-w-0">
          {/* Header */}
          <header className="h-16 bg-[#F4F5F0] border-b border-slate-200/60 flex items-center justify-between px-4 lg:px-6 shrink-0 z-10">
            <div className="flex items-center gap-3 lg:gap-6">
              {/* Mobile Sidebar Trigger */}
              <div className="lg:hidden">
                <Sheet>
                  <SheetTrigger
                    render={
                      <Button variant="ghost" size="icon" className="h-9 w-9 text-slate-500">
                        <Menu className="w-5 h-5" />
                      </Button>
                    }
                  />
                  <SheetContent side="left" className="p-0 w-64 bg-[#F4F5F0]">
                    <Sidebar isCollapsed={false} onToggle={() => {}} />
                  </SheetContent>
                </Sheet>
              </div>

              <h1 className="text-lg lg:text-xl font-bold tracking-tight text-slate-800 flex items-center gap-2 lg:hidden">
                <span className="bg-blue-600 text-white p-1 rounded-md text-xs">AI</span>
                NADOCEO
              </h1>

              <div className="hidden lg:flex items-center gap-3">
                <span className="text-sm font-semibold text-slate-500">현재 코칭</span>
                <Separator orientation="vertical" className="h-4 bg-slate-300" />
                <Button variant="ghost" size="sm" className="text-slate-800 font-bold hover:bg-slate-200/50 gap-2 h-9 px-4 rounded-xl border border-slate-200 bg-white shadow-sm">
                  Java 기초
                  <ChevronDown className="w-4 h-4 opacity-50" />
                </Button>
              </div>
            </div>

            <div className="flex items-center gap-2 lg:gap-4">
              <div className="flex items-center gap-2">
                <Avatar className="w-8 h-8 border border-white shadow-sm bg-blue-100">
                  <AvatarFallback className="bg-transparent text-blue-700">
                    <User className="w-4 h-4" />
                  </AvatarFallback>
                </Avatar>
                <span className="text-sm font-medium text-slate-700 hidden lg:inline">{displayName}</span>
              </div>
              <Separator orientation="vertical" className="h-6 bg-slate-300 hidden lg:block" />
              <Button
                variant="ghost"
                size="sm"
                onClick={handleLogout}
                className="text-slate-500 hover:text-red-600 hover:bg-red-50 gap-2 h-8"
              >
                <LogOut className="w-4 h-4" />
                <span className="hidden lg:inline">로그아웃</span>
              </Button>
            </div>
          </header>

          {/* Chat Split View */}
          <div className="flex-1 flex flex-col lg:flex-row min-h-0 bg-[#F4F5F0]">
            <div className="flex-1 lg:w-[60%] lg:min-w-[400px] border-b lg:border-b-0 lg:border-r border-slate-200/60 z-10 flex flex-col h-[60vh] lg:h-auto">
              <MainChatPanel />
            </div>
            <div className="flex-1 lg:w-[40%] lg:min-w-[300px] bg-white flex flex-col h-[40vh] lg:h-auto">
              <SubChatPanel />
            </div>
          </div>

          {/* Footer */}
          <LearningPathBar />
        </div>
      </div>
    </TooltipProvider>
  );
}

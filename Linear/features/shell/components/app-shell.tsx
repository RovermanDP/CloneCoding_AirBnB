import type { ReactNode } from "react";
import { AppSidebar } from "@/features/shell/components/app-sidebar";
import { Topbar } from "@/features/shell/components/topbar";

export function AppShell({ children }: { children: ReactNode }) {
  return (
    <div className="app-shell">
      <div className="shell-grid">
        <AppSidebar />
        <main className="page-surface">
          <Topbar />
          {children}
        </main>
      </div>
    </div>
  );
}


import type { ReactNode } from "react";
import { ButlerProvider } from "@/features/butler/state/butler-provider";
import { AppShell } from "@/features/shell/components/app-shell";

export default function ButlerLayout({ children }: { children: ReactNode }) {
  return (
    <ButlerProvider>
      <AppShell>{children}</AppShell>
    </ButlerProvider>
  );
}

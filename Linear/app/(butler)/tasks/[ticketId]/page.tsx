import { notFound } from "next/navigation";
import { TaskDetail } from "@/features/tasks/components/task-detail";

export default async function TaskDetailPage({
  params
}: {
  params: Promise<{ ticketId: string }>;
}) {
  const { ticketId } = await params;
  if (!ticketId) notFound();
  return <TaskDetail ticketId={ticketId} />;
}

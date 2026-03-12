import { useEffect, useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useAuth } from "@/context/AuthContext";
import { authService } from "@/services/authService";
import { queryKeys } from "@/lib/queryKeys";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Switch } from "@/components/ui/switch";
import { useToast } from "@/hooks/use-toast";

const Settings = () => {
  const { user, updateProfile, refreshUser } = useAuth();
  const { toast } = useToast();
  const queryClient = useQueryClient();

  const [name, setName] = useState(user?.name ?? "");
  const [email, setEmail] = useState(user?.email ?? "");
  const [darkMode, setDarkMode] = useState(user?.darkMode ?? false);
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");

  // Fetch latest profile from server
  const { data: profile } = useQuery({
    queryKey: queryKeys.userProfile,
    queryFn: authService.getProfile,
  });

  // Sync form when profile loads
  useEffect(() => {
    if (profile) {
      setName(profile.name);
      setEmail(profile.email);
      setDarkMode(profile.darkMode);
    }
  }, [profile]);

  const profileMutation = useMutation({
    mutationFn: (data: Parameters<typeof authService.updateProfile>[0]) => authService.updateProfile(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.userProfile });
      refreshUser();
      toast({ title: "Saved", description: "Profile updated successfully" });
    },
    onError: () => toast({ title: "Error", description: "Failed to update profile", variant: "destructive" }),
  });

  const handleSave = () => {
    profileMutation.mutate({ name, email });
  };

  const handlePasswordChange = () => {
    if (!currentPassword || !newPassword) {
      toast({ title: "Validation Error", description: "Please fill both password fields", variant: "destructive" });
      return;
    }
    profileMutation.mutate({ password: newPassword });
    setCurrentPassword("");
    setNewPassword("");
  };

  const handleDarkMode = (checked: boolean) => {
    setDarkMode(checked);
    document.documentElement.classList.toggle("dark", checked);
    profileMutation.mutate({ darkMode: checked });
  };

  return (
    <div className="space-y-6 animate-fade-in max-w-2xl">
      <h1 className="text-2xl font-bold font-display">Settings</h1>

      <Card>
        <CardHeader>
          <CardTitle className="text-sm">Profile</CardTitle>
          <CardDescription className="text-xs">Update your personal details</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="space-y-1">
            <Label className="text-xs">Full Name</Label>
            <Input value={name} onChange={(e) => setName(e.target.value)} />
          </div>
          <div className="space-y-1">
            <Label className="text-xs">Email</Label>
            <Input value={email} onChange={(e) => setEmail(e.target.value)} />
          </div>
          <Button onClick={handleSave} size="sm" disabled={profileMutation.isPending}>Save Changes</Button>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle className="text-sm">Password</CardTitle>
          <CardDescription className="text-xs">Change your account password</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="space-y-1">
            <Label className="text-xs">Current Password</Label>
            <Input type="password" value={currentPassword} onChange={(e) => setCurrentPassword(e.target.value)} />
          </div>
          <div className="space-y-1">
            <Label className="text-xs">New Password</Label>
            <Input type="password" value={newPassword} onChange={(e) => setNewPassword(e.target.value)} />
          </div>
          <Button size="sm" variant="outline" onClick={handlePasswordChange} disabled={profileMutation.isPending}>
            Update Password
          </Button>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle className="text-sm">Preferences</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium">Dark Mode</p>
              <p className="text-xs text-muted-foreground">Toggle dark theme</p>
            </div>
            <Switch checked={darkMode} onCheckedChange={handleDarkMode} />
          </div>
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium">Currency</p>
              <p className="text-xs text-muted-foreground">Currently: {profile?.currency ?? user?.currency ?? "INR"}</p>
            </div>
            <Button variant="outline" size="sm" disabled>
              {profile?.currency ?? user?.currency ?? "INR"}
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default Settings;

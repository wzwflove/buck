/*
 * Copyright 2017-present Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.facebook.buck.rules;

import com.facebook.buck.io.filesystem.ProjectFilesystem;
import com.facebook.buck.model.BuildTarget;
import com.facebook.buck.util.MoreCollectors;
import com.facebook.buck.util.immutables.BuckStyleImmutable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import org.immutables.value.Value;

public class FakeBinaryBuildRuleBuilder
    extends AbstractNodeBuilder<
        FakeBinaryArg.Builder, FakeBinaryArg,
        FakeBinaryBuildRuleBuilder.FakeBinaryBuildRuleDescription, FakeBinaryBuildRule> {
  private static FakeBinaryBuildRuleDescription fakeBinaryBuildRuleDescription =
      new FakeBinaryBuildRuleDescription();

  public FakeBinaryBuildRuleBuilder(BuildTarget target) {
    super(fakeBinaryBuildRuleDescription, target);
  }

  public FakeBinaryBuildRuleBuilder setCommand(Iterable<String> command) {
    getArgForPopulating().setCommand(command);
    return this;
  }

  public FakeBinaryBuildRuleBuilder setDeps(ImmutableSortedSet<BuildTarget> deps) {
    getArgForPopulating().setDeps(deps);
    return this;
  }

  public FakeBinaryBuildRuleBuilder setEnvironment(ImmutableSortedMap<String, String> environment) {
    getArgForPopulating().setEnvironment(environment);
    return this;
  }

  public FakeBinaryBuildRuleBuilder setInputs(ImmutableSortedSet<SourcePath> inputs) {
    getArgForPopulating().setInputs(inputs);
    return this;
  }

  static class FakeBinaryBuildRuleDescription implements Description<FakeBinaryArg> {

    @Override
    public Class<com.facebook.buck.rules.FakeBinaryArg> getConstructorArgType() {
      return FakeBinaryArg.class;
    }

    @Override
    public BuildRule createBuildRule(
        TargetGraph targetGraph,
        BuildTarget buildTarget,
        ProjectFilesystem projectFilesystem,
        BuildRuleParams params,
        BuildRuleResolver resolver,
        CellPathResolver cellRoots,
        FakeBinaryArg args) {
      return new FakeBinaryBuildRule(
          buildTarget,
          projectFilesystem,
          resolver.getAllRules(args.getDeps()),
          new AbstractTool() {
            @AddToRuleKey private final ImmutableList<String> command = args.getCommand();

            @AddToRuleKey
            private final ImmutableSortedMap<String, String> environment = args.getEnvironment();

            @AddToRuleKey private final ImmutableSortedSet<SourcePath> inputs = args.getInputs();

            @AddToRuleKey
            private final ImmutableList<NonHashableSourcePathContainer> depsInputs =
                args.getDeps()
                    .stream()
                    .map(
                        target ->
                            new NonHashableSourcePathContainer(
                                DefaultBuildTargetSourcePath.of(target)))
                    .collect(MoreCollectors.toImmutableList());

            @Override
            public ImmutableList<String> getCommandPrefix(SourcePathResolver resolver1) {
              return command;
            }

            @Override
            public ImmutableMap<String, String> getEnvironment(SourcePathResolver resolver1) {
              return args.getEnvironment();
            }
          });
    }
  }

  @BuckStyleImmutable
  @Value.Immutable
  interface AbstractFakeBinaryArg extends CommonDescriptionArg, HasDeclaredDeps {
    ImmutableList<String> getCommand();

    @Value.NaturalOrder
    @Override
    ImmutableSortedSet<BuildTarget> getDeps();

    @Value.NaturalOrder
    ImmutableSortedSet<SourcePath> getInputs();

    @Value.NaturalOrder
    ImmutableSortedMap<String, String> getEnvironment();
  }
}
